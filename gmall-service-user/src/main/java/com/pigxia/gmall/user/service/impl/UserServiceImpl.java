package com.pigxia.gmall.user.service.impl;




import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pigxia.gmall.bean.UmsMember;
import com.pigxia.gmall.bean.UmsMemberReceiveAddress;
import com.pigxia.gmall.service.UserService;
import com.pigxia.gmall.user.mapper.UserDao;
import com.pigxia.gmall.user.mapper.UserMembersDao;
import com.pigxia.gmall.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

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

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<UmsMember> getAllUsers() {
        List<UmsMember> memberList=userDao.selectAll();
        return memberList;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {
        UmsMemberReceiveAddress memberReceiveAddress=new UmsMemberReceiveAddress();
        memberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> receiveAddresses=membersDao.select(memberReceiveAddress);
        return receiveAddresses;
    }


    public UmsMember login(UmsMember umsMember) {
         Jedis jedis = redisUtil.getJedis();
         if (jedis!=null){
             String user=jedis.get("user:"+umsMember.getPassword()+umsMember.getUsername()+":info");
             if (user!=null){
                 // 从缓存中查到了用户数据
                 UmsMember umsMember1 = JSON.parseObject(user, UmsMember.class);
                 return umsMember1;
             }
         }
        // 没有查到或者jedis宕机了从db中查询
        UmsMember umsMember1=searchFromDb(umsMember);
        if (umsMember1!=null){
            if(jedis!=null){
                // 从数据库中查到了数据，放入缓存redis中  使用用户名+密码作为key，为了防止key被覆盖
                jedis.setex("user:"+umsMember.getPassword()+umsMember.getUsername()+":info",60*60*24, JSON.toJSONString(umsMember1));
            }
        }
        // 可能为空，可能有值，数据库中都不存在，那就是用户信息不对
        return umsMember1;
    }

    // 存入一份token到redis
    public void addUserTokenCache(String token, String memberId) {
        Jedis jedis =null;
        try {
             jedis=redisUtil.getJedis();
            if (jedis!=null){
                jedis.setex("user:"+memberId+":info",60*60*2,token);
            }
        } finally {
            if (jedis!=null){
                jedis.close();
            }
        }
    }

    @Override
    public UmsMember insertOauth2(UmsMember umsMember) {
        userDao.insertSelective(umsMember);
        return umsMember;
    }

    @Override
    public UmsMember UmsMemberCheck(UmsMember umsMemberCheck) {
         UmsMember umsMember = userDao.selectOne(umsMemberCheck);
        return umsMember;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String deliveryAddressId) {
        UmsMemberReceiveAddress address = new UmsMemberReceiveAddress();
        address.setId(deliveryAddressId);
        UmsMemberReceiveAddress address1 = membersDao.selectOne(address);
        return address1;
    }

    private UmsMember searchFromDb(UmsMember umsMember) {
        // 按理一个用户根据用户名和密码查询出的数据只有一条，为了严谨一点
         List<UmsMember> umsMemberLists = userDao.select(umsMember);
         umsMember=umsMemberLists.get(0);
         return umsMember;
    }
}
