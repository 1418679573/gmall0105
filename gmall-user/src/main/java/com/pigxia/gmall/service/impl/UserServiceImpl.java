package com.pigxia.gmall.service.impl;



import com.pigxia.gmall.bean.UmsMember;
import com.pigxia.gmall.bean.UmsMemberReceiveAddress;
import com.pigxia.gmall.mapper.UserDao;
import com.pigxia.gmall.mapper.UserMembersDao;
import com.pigxia.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by absen on 2020/5/26 19:04
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    UserMembersDao membersDao;
    @Override
    public List<UmsMember> getAllUsers() {
        List<UmsMember> memberList=userDao.selectAll();
        return memberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        UmsMemberReceiveAddress memberReceiveAddress=new UmsMemberReceiveAddress();
        memberReceiveAddress.setId(memberId);
        List<UmsMemberReceiveAddress> receiveAddresses=membersDao.select(memberReceiveAddress);
        return receiveAddresses;
    }
}
