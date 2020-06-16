package com.pigxia.gmall.order.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.pigxia.gmall.bean.OmsOrder;
import com.pigxia.gmall.bean.OmsOrderItem;
import com.pigxia.gmall.order.mapper.OrderItemServiceDao;
import com.pigxia.gmall.order.mapper.OrderServiceDao;
import com.pigxia.gmall.service.OrderService;
import com.pigxia.gmall.utils.ActiveMQUtil;
import com.pigxia.gmall.utils.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import sun.plugin2.os.windows.SECURITY_ATTRIBUTES;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by absen on 2020/6/9 15:24
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderServiceDao orderServiceDao;
    @Autowired
    OrderItemServiceDao orderItemServiceDao;

    @Autowired
    ActiveMQUtil activeMQUtil;
    @Override
    public String checkTradeCode(String memberId,String tradeCode) {
        Jedis jedis = null;
        String success = "fail";
        try {
            jedis = redisUtil.getJedis();
            String tradeKey="user:" + memberId + "tradeCode";
            String tradeCodeFromCache=jedis.get(tradeKey);
            //对比防重删令牌   使用redis+lua脚本在查询到key的时候就在redis中删除该值，防止高并发重复提交多个表单
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
            if (eval!=0){
                success="success";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return success;
    }

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeCode = "";
        try {
            jedis = redisUtil.getJedis();
            tradeCode = UUID.randomUUID().toString();
            jedis.setex("user:" + memberId + "tradeCode", 60 * 15, tradeCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return tradeCode;
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        // 保存订单表
       orderServiceDao.insertSelective(omsOrder);
       // 保存订单详情表
        List<OmsOrderItem> orderItemList = omsOrder.getOrderItemList();
        for (OmsOrderItem omsOrderItem : orderItemList) {
             omsOrderItem.setOrderId(omsOrder.getId());
            orderItemServiceDao.insertSelective(omsOrderItem);
            // 删除购物车中的商品信息 可以使用reference调用cartService
        }
    }

    @Override
    public OmsOrder getOrderByMemberId(String memberId) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setMemberId(memberId);
        List<OmsOrder> omsOrders=orderServiceDao.select(omsOrder);
        omsOrder=omsOrders.get(omsOrders.size()-1);
        return omsOrder;
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder order = new OmsOrder();
        order.setOrderSn(outTradeNo);
        order=orderServiceDao.selectOne(order);
        return order;
    }

    @Override
    public void updateOrderByOrderSn(OmsOrder order) {
        Example example=new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",order.getOrderSn());
        // 修改订单服务为已支付
        order.setStatus("1");
        // 订单服务完成需要发送一个订单已完成的消息到mq，库存消费该消息，并发开启库存服务
        Connection connection = null;
        Session session=null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session= connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_success_queue = session.createQueue("ORDER_SUCCESS_QUEUE");
            MapMessage message=new ActiveMQMapMessage();
            message.setString("","");
            MessageProducer producer = session.createProducer(order_success_queue);
            orderServiceDao.updateByExampleSelective(order,example);
            producer.send(message);
            session.commit();
        } catch (JMSException e) {
            e.printStackTrace();
            try {
                session.close();
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

}
