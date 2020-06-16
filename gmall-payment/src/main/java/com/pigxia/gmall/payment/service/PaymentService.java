package com.pigxia.gmall.payment.service;

import com.pigxia.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * Created by absen on 2020/6/10 18:38
 */
public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePayment(PaymentInfo paymentInfo);

    void sendDelayPaymentResult(String out_trade_no,int count);

    Map<String,Object> checkAlipayPayment(String outTradeNo);
}
