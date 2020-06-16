package com.pigxia.gmall.payment.service.impl;



import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.pigxia.gmall.bean.PaymentInfo;
import com.pigxia.gmall.payment.mapper.PaymentServiceDao;
import com.pigxia.gmall.payment.service.PaymentService;
import com.pigxia.gmall.utils.ActiveMQUtil;
import javafx.beans.binding.ObjectExpression;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by absen on 2020/6/10 18:38
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentServiceDao paymentServiceDao;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    AlipayClient alipayClient;
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentServiceDao.insertSelective(paymentInfo);
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {
        // 此时需要进行一个幂等性的检查，执行到更改支付状态有两种可能
        // 1.通过支付成功然后支付宝回调进行更改支付状态
        //2.通过延时队列去调用支付宝的检查接口（alipay.trade.query(统一收单线下交易查询) ）检查支付宝中我们的订单的状态
        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        paymentInfoParam=paymentServiceDao.selectOne(paymentInfoParam);
        String paymentStatus=paymentInfoParam.getPaymentStatus();
        if (StringUtils.isNotBlank(paymentStatus)&&paymentStatus.equals("已支付")){
            return;
        }

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",paymentInfo.getOrderSn());
        Connection connection = null;
        Session session=null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session= connection.createSession(true, Session.SESSION_TRANSACTED);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0

        try {
            paymentServiceDao.updateByExampleSelective(paymentInfo,example);
            payment_success_Queue(paymentInfo,session);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public Map<String, Object> checkAlipayPayment(String outTradeNo) {
         HashMap<String, Object> resultMessage = new HashMap<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String, Object> map = new HashMap<>();
         map.put("out_trade_no",outTradeNo);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功，支付订单生成，还存在未付款和已付款的可能");
            resultMessage.put("trade_status",response.getTradeStatus());
            resultMessage.put("trade_no",response.getTradeNo());
            resultMessage.put("out_trade_no",response.getOutTradeNo());
            resultMessage.put("call_back_content",response.getMsg());
        } else {
            System.out.println("订单可能还未生成，调用失败");
        }
        return resultMessage;
    }

    @Override
    public void sendDelayPaymentResult(String out_trade_no,int count) {
        Connection connection = null;
        Session session=null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session= connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_success_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MapMessage message=new ActiveMQMapMessage();
            message.setString("outTradeNo",out_trade_no);
            message.setInt("checkCount",count);
            // 设置延时队列，每10秒创建一次延时队列
            message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*10);
            MessageProducer producer = session.createProducer(order_success_queue);
            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
            try {
                session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        }finally {
            try {
                session.close();
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }

    private void payment_success_Queue(PaymentInfo paymentInfo,Session session) throws JMSException {
        Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
        MessageProducer producer = session.createProducer(payment_success_queue);
        MapMessage message=new ActiveMQMapMessage(); //hash结构
        message.setString("OrderSn",paymentInfo.getOrderSn());
        producer.send(message);
    }


}
