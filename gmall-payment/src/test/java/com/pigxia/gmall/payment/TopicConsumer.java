package com.pigxia.gmall.payment;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by absen on 2020/6/11 14:53
 */
public class TopicConsumer {
    public static void main(String[] args) {
        ConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER, ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
        try {
            Connection connection = connect.createConnection();
            connection.setClientID("aa");//主题消息子在客户端持久化id
            connection.start();
            //第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic("speaking");

            MessageConsumer consumer = session.createDurableSubscriber(topic,"aa");
            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message instanceof TextMessage) {
                        try {
                            String text = ((TextMessage) message).getText();
                            System.out.println(text + "主题模式消费者");

                            //session.rollback();
                        } catch (JMSException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
    }


}
