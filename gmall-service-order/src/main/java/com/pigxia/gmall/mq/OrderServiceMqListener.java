package com.pigxia.gmall.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.bean.OmsOrder;
import com.pigxia.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * Created by absen on 2020/6/11 17:02
 */
@Component
public class OrderServiceMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE",containerFactory = "jmsQueueListener")
    public void ConsumerPaymentQueue(MapMessage message) throws JMSException {

        String orderSn=message.getString("OrderSn");
         OmsOrder order = new OmsOrder();
         order.setOrderSn(orderSn);
         orderService.updateOrderByOrderSn(order);
    }

}
