package com.pigxia.gmall.payment;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * Created by absen on 2020/6/11 14:52
 */
public class TestTopic {
    public static void main(String[] args) {

        ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);  //开启事物
            Topic topic = session.createTopic("speaking"); // 主题模式的消息
            //Topic topic=session.createTopic("");  // 主题模式的消息

            MessageProducer producer = session.createProducer(topic);
            TextMessage textMessage=new ActiveMQTextMessage();
            textMessage.setText("爱拼才会赢，大家一起努力！");
            producer.setDeliveryMode(DeliveryMode.PERSISTENT); //PERSISTENT 持久化
            producer.send(textMessage);
            session.commit(); // 事物提交
            connection.close();

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
