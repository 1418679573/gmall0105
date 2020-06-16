package com.pigxia.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pigxia.gmall.bean.*;
import com.pigxia.gmall.manager.mapper.PmsSkuAttrValueDao;
import com.pigxia.gmall.manager.mapper.PmsSkuImageDao;
import com.pigxia.gmall.manager.mapper.PmsSkuInfoDao;
import com.pigxia.gmall.manager.mapper.PmsSkuSaleAttrValueDao;
import com.pigxia.gmall.service.SkuService;
import com.pigxia.gmall.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by absen on 2020/5/29 14:43
 */
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoDao skuInfoDao;

    @Autowired
    PmsSkuAttrValueDao skuAttrValueDao;


    @Autowired
    PmsSkuSaleAttrValueDao skuSaleAttrValueDao;


    @Autowired
    PmsSkuImageDao skuImageDao;

   @Autowired
    RedisUtil redisUtil;
    //  存储一个sku 商品
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

       //  pmsSkuInfo.getId();
        //  存储商品信息
        skuInfoDao.insertSelective(pmsSkuInfo);

        // 存储商品的平台销售属性值
        List<PmsSkuAttrValue> skuAttrValues = pmsSkuInfo.getSkuAttrValueList();
        if (skuAttrValues != null) {
            for (PmsSkuAttrValue skuAttrValue : skuAttrValues) {
                skuAttrValue.setSkuId(pmsSkuInfo.getId());
                skuAttrValueDao.insertSelective(skuAttrValue);
            }
        }
        // 存储商品的商家的销售属性

        List<PmsSkuSaleAttrValue> skuSaleAttrValues = pmsSkuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValues != null) {
            for (PmsSkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValues) {
                skuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
                skuSaleAttrValueDao.insertSelective(skuSaleAttrValue);
            }
        }
        // 存储图片信息
        List<PmsSkuImage> skuImages = pmsSkuInfo.getSkuImageList();
        if (skuImages != null) {
            for (PmsSkuImage skuImage : skuImages) {
                skuImage.setSkuId(pmsSkuInfo.getId());
                skuImageDao.insert(skuImage);
            }
        }
        return "success";
    }

    // 根据商品id 得到一个具体的商品
    public PmsSkuInfo getSkuInfo(String skuId,String ip) {
        //  连接redis
        Jedis jedis = redisUtil.getJedis();
        System.out.println(jedis);
        // 查询redis
        String key = "sku:" + skuId + ":info";
        String value = jedis.get(key);
        PmsSkuInfo pmsSkuInfo;

        String token= UUID.randomUUID().toString(); // token用来判断删除的是自己分布式锁的key，
        // 当然也可以不删除对应点儿科，让通过过期时间自动删除。但是为了性能上的考虑，代码删除

        // 设置分布式锁，为了防止缓存击穿（即多个并发对一个在redis中不存在，都直接访问数据库）
        // 10s过期时间
        String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "ex", 10);
        if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
            System.out.println("ip为:"+ip+"的用户;"+Thread.currentThread().getName()+"获得了分布式锁！===================");
            //  设置好了分布式锁
            // 进行判断是否存在 redis中，没有redis则查询数据库
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (StringUtils.isNotBlank(value)) {
                System.out.println("ip为:"+ip+"的用户;"+Thread.currentThread().getName()+"直接从缓存中获取到了数据！-----------");
                pmsSkuInfo = JSON.parseObject(value, PmsSkuInfo.class);
            } else {
                // 查询数据库
                pmsSkuInfo = getPmsSkuInfoByDb(skuId);
                value = JSON.toJSONString(pmsSkuInfo);
                if (pmsSkuInfo != null) {
                    // 查询mysql并且有结果，将结果放到redis中
                    jedis.set(key, value);
                } else {
                    //     mysql数据库中也不存在
                    // 为了防止缓存穿透，即多次对mysql查询有个不存在的数据，设置一个空值，时间一般不长设5分钟
                    jedis.setex(key, 60 * 5, "");
                }
                System.out.println("ip为:"+ip+"的用户;"+Thread.currentThread().getName()+"查询数据库===========");
            }
            // 在访问mysql后，释放分布式锁
            String lockToken=jedis.get("sku:" + skuId + ":lock"); // 判断是否是自己的分布式锁

            //对比防重删令牌   使用redis+lua脚本在查询到key的时候就在redis中删除该值，防止高并发重复提交多个表单
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(lockToken),
                    Collections.singletonList(token));
            if(eval!=0){
                //  可能刚好在自己的分布式锁进行判断完了，进入到了该行，但是刚好过期，此时还是删的是别人的分布式锁
               //   jedis.eval()   此时可以使用lua脚本，在获取是自己的分布式锁的同时删除该分布锁
                System.out.println("ip为:"+ip+"的用户;"+Thread.currentThread().getName()+"删除了分布式锁++++++");
            }
        } else {
            System.out.println("ip为:"+ip+"的用户;"+Thread.currentThread().getName()+"没有获取到分布式锁，进行了自旋-------------");
            // 设置失败，进行自旋
            // 一定加return，保证是同一个线程。 否则就是两个不一样的线程
            try {
                // 睡3s
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getSkuInfo(skuId,ip);

        }
        // 关闭资源
        jedis.close();
        //将结果展示到前端
        return pmsSkuInfo;
    }

    private PmsSkuInfo getPmsSkuInfoByDb(String skuId) {
        // 查询商品信息
        PmsSkuInfo skuInfo = new PmsSkuInfo();
        skuInfo.setId(skuId);
        PmsSkuInfo pmsSkuInfo = skuInfoDao.selectOne(skuInfo);
        //  查询商品的集合图片信息,放入sku对象中
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> skuImageList = skuImageDao.select(pmsSkuImage);
        pmsSkuInfo.setSkuImageList(skuImageList);
        return pmsSkuInfo;
    }

    //查询出这一sku系列的所有sku集合，进行转化为一个hashMap的json串，放到前台，便于快速切换
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfoList=skuInfoDao.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfoList;
    }

    @Override
    public List<PmsSkuInfo> getAll(String s) {
         List<PmsSkuInfo> pmsSkuInfoList = skuInfoDao.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
             PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
             pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
             List<PmsSkuAttrValue> select = skuAttrValueDao.select(pmsSkuAttrValue);
             pmsSkuInfo.setSkuAttrValueList(select);
        }
        return pmsSkuInfoList;
    }

    @Override
    public boolean checkPrice(String productSkuId, BigDecimal price) {
        boolean b=false;
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);

        PmsSkuInfo pmsSkuInfo1 = skuInfoDao.selectOne(pmsSkuInfo);
        if (pmsSkuInfo1.getPrice().compareTo(price)==0){
            return true;
        }
        return false;
    }
}
