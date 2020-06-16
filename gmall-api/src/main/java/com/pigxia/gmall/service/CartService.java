package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * Created by absen on 2020/6/4 19:40
 */
public interface CartService {
    OmsCartItem ifCartExitsByUser(String memberId, String skuId);

    List<OmsCartItem> flushCache(String memberId);

    void updateCart(OmsCartItem cartItemFromDb);

    void addCart(OmsCartItem cartItem);

    List<OmsCartItem> cartList(String memberId);

    void checkCart(OmsCartItem omsCartItem);
}
