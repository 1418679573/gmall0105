package com.pigxia.gmall.payment.mq;

import com.pigxia.gmall.bean.PaymentInfo;
import com.pigxia.gmall.payment.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

/**
 * Created by absen on 2020/6/12 1:50
 */

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeCheckResult(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("outTradeNo");
        int checkCount = mapMessage.getInt("checkCount");

        // 调用paymenService的支付宝检查接口
        System.out.println("进行延时检查，调用检查支付宝的订单状态的接口");
        Map<String, Object> resultMessage = paymentService.checkAlipayPayment(outTradeNo);
        //  不能无限的调用延时队列，设置一个次数，超过这个次数则表示支付失败，采用补偿性策略，例如人工处理
        if (checkCount > 0) {
            if (resultMessage == null || resultMessage.isEmpty()) {
                System.out.println("继续调用延时队列，对支付宝订单接口进行检查");
                paymentService.sendDelayPaymentResult(outTradeNo, checkCount - 1);
            } else {
                // 获得支付状态
                String trade_status = (String) resultMessage.get("trade_status");
                if (StringUtils.isNotBlank(trade_status)&&trade_status.equals("TRADE_SUCCESS")) {
                    // 支付状态检测为支付成功
                    //  去调用支付服务，修改支付信息和发送支付成功的消息，并发开启订单服务
                    PaymentInfo paymentInfo = new PaymentInfo();
                    paymentInfo.setPaymentStatus("已付款");
                    paymentInfo.setCallbackContent((String) resultMessage.get("call_back_content"));
                    paymentInfo.setAlipayTradeNo((String) resultMessage.get("trade_no"));
                    paymentInfo.setOrderSn((String) resultMessage.get("out_trade_no"));
                    paymentInfo.setCallbackTime(new Date());
                    paymentService.updatePayment(paymentInfo);
                    System.out.println("调用支付服务，修改支付信息和发送支付成功的消息，并发开启订单服务");
                } else {
                    System.out.println("没有支付成功。继续调用延时队列");
                    paymentService.sendDelayPaymentResult(outTradeNo, checkCount - 1);
                }
            }
        }
    }
}
