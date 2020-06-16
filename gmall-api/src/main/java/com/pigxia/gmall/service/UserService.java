package com.pigxia.gmall.service;


import com.pigxia.gmall.bean.UmsMember;
import com.pigxia.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * Created by absen on 2020/5/26 19:04
 */
public interface UserService {
    List<UmsMember> getAllUsers();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMember login(UmsMember umsMember);

    void addUserTokenCache(String token, String memberLevelId);

    UmsMember insertOauth2(UmsMember umsMember);

    UmsMember UmsMemberCheck(UmsMember umsMemberCheck);

    UmsMemberReceiveAddress getReceiveAddressById(String deliveryAddressId);
}
