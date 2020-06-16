package com.pigxia.gmall.cart.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pigxia.gmall.cart.mapper.CartServiceDao;
import com.pigxia.gmall.bean.OmsCartItem;
import com.pigxia.gmall.service.CartService;
import com.pigxia.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by absen on 2020/6/4 19:41
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartServiceDao cartServiceDao;

    @Autowired
    RedisUtil redisUtil;
    @Override
    public OmsCartItem ifCartExitsByUser(String memberId, String skuId) {
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
         OmsCartItem omsCartItem1 = cartServiceDao.selectOne(omsCartItem);
        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem cartItem) {
        if (StringUtils.isNotBlank(cartItem.getMemberId())) {
            cartServiceDao.insert(cartItem);
            // 进行缓存的同步
            flushCache(cartItem.getMemberId());
        }
    }

    @Override
    public void updateCart(OmsCartItem cartItemFromDb) {
         Example example = new Example(OmsCartItem.class);
         example.createCriteria().andEqualTo("id",cartItemFromDb.getId());
         cartServiceDao.updateByExampleSelective(cartItemFromDb,example);
        // 进行缓存的同步
        flushCache(cartItemFromDb.getMemberId());
    }



   // 进行数据的缓存
    public  List<OmsCartItem>  flushCache(String memberId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItems=null;
        try {
            jedis = redisUtil.getJedis();
            OmsCartItem omsCartItem=new OmsCartItem();
            omsCartItem.setMemberId(memberId);
            //  根据memberId查询购物车数据
            omsCartItems = cartServiceDao.select(omsCartItem);
            //  计算下每种类型的商品的总价格  单价乘以总数量
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            }
            // 删除原来的数据,在进行缓存
            jedis.del("user:"+memberId+":cart");
            jedis.hset("user:"+memberId+":cart", memberId+"",JSON.toJSONString(omsCartItems));
        } catch (Exception e) {
            e.printStackTrace();
            // 调用日志服务进行记录日志
            String errorMessage=e.getMessage();
            // logService.log
        } finally {
            jedis.close();
        }
        return omsCartItems;
    }

   //  查询购物车清单
    public List<OmsCartItem> cartList(String memberId) {
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        Jedis jedis =null;
        try {
           jedis=redisUtil.getJedis();//    "user:"+memberId+":cart"
           String cart = jedis.hget("user:"+memberId+":cart",memberId+"");
              omsCartItems = JSONArray.parseArray(cart, OmsCartItem.class);
              if (omsCartItems==null){
                  omsCartItems= flushCache(memberId);
              }
            //  计算下每种类型的商品的总价格  单价乘以总数量
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return omsCartItems;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {
        Example e=new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId()).andEqualTo("productSkuId",omsCartItem.getProductSkuId());
        // 根据参数进行查询，有值的才修改，无值的不动
        cartServiceDao.updateByExampleSelective(omsCartItem,e);
        // 进行缓存的同步
        flushCache(omsCartItem.getMemberId());
    }
}
