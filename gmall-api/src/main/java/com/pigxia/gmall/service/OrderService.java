package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.OmsOrder;

import java.math.BigDecimal; /**
 * Created by absen on 2020/6/9 15:19
 */
public interface OrderService {
    String checkTradeCode(String memberId,String tradeCode);

    String genTradeCode(String memberId);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByMemberId(String memberId);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrderByOrderSn(OmsOrder order);
}
