package com.pigxia.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pigxia.gmall.bean.PmsProductImage;
import com.pigxia.gmall.bean.PmsProductInfo;
import com.pigxia.gmall.bean.PmsProductSaleAttr;
import com.pigxia.gmall.bean.PmsProductSaleAttrValue;
import com.pigxia.gmall.manager.mapper.PmsProductImageDao;
import com.pigxia.gmall.manager.mapper.PmsProductInfoDao;
import com.pigxia.gmall.manager.mapper.PmsProductSaleAttrDao;
import com.pigxia.gmall.manager.mapper.PmsProductSaleAttrValueDao;
import com.pigxia.gmall.service.SpuService;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by absen on 2020/5/28 13:29
 */
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductInfoDao pmsProductInfoDao;
    @Autowired
    PmsProductImageDao pmsProductImageDao;
    @Autowired
    PmsProductSaleAttrDao pmsProductSaleAttrDao;
    @Autowired
    PmsProductSaleAttrValueDao pmsProductSaleAttrValueDao;
    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo=new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoDao.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        // 进行商品的插入，对象里的属性为空值则不插入
        pmsProductInfoDao.insertSelective(pmsProductInfo);

        // 根据主键返回策略，需要将商品的id，分别给图片，属性名称，属性值设置商品的id

        //  图片插入 赋予对应的商品id
         List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(pmsProductInfo.getId());
            pmsProductImageDao.insertSelective(pmsProductImage);
        }

        // 销售属性名称插入
         List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
            pmsProductSaleAttr.setProductId(pmsProductInfo.getId());
            pmsProductSaleAttrDao.insertSelective(pmsProductSaleAttr);
            // 得到该属性名称下属性值，赋予对应的商品id在将属性值进行插入
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(pmsProductSaleAttr.getProductId());
                pmsProductSaleAttrValueDao.insertSelective(pmsProductSaleAttrValue);
            }
        }
        return "success";
    }

    //  根据spuId 查询出对应的销售属性
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        // 双重集合 首先查询出销售属性名
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
         List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductSaleAttrDao.select(pmsProductSaleAttr);
        // 根据销售属性名查询出销售属性值
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrList) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> productSaleAttrValueList = pmsProductSaleAttrValueDao.select(pmsProductSaleAttrValue);
            // 将销售属性值存入销售属性中
            productSaleAttr.setSpuSaleAttrValueList(productSaleAttrValueList);
        }
        return pmsProductSaleAttrList;
    }

    //  查询出图片信息，用于显示
    public List<PmsProductImage> spuImageList(String spuId) {
         PmsProductImage pmsProductImage = new PmsProductImage();
         pmsProductImage.setProductId(spuId);
        List<PmsProductImage> productImageList = pmsProductImageDao.select(pmsProductImage);
        return productImageList;
    }

    //查询出当前的商品的属性和属性值，放在商品的详情页，并且对当前属性和属性值的选中进行标记
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {
//         PmsProductSaleAttr productSaleAttr = new PmsProductSaleAttr();
//         productSaleAttr.setProductId(productId);
//         // 根据 spuId 查询出对应的属性
//        List<PmsProductSaleAttr> productSaleAttrs = pmsProductSaleAttrDao.select(productSaleAttr);
//        for (PmsProductSaleAttr saleAttr : productSaleAttrs) {
//            //  再根据spuId 和saleId 查出对应的属性值
//            PmsProductSaleAttrValue saleAttrValue=new PmsProductSaleAttrValue();
//            saleAttrValue.setProductId(productId);
//            saleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
//             List<PmsProductSaleAttrValue> saleAttrValueList = pmsProductSaleAttrValueDao.select(saleAttrValue);
//           // 将值放入到spu商品的销售属性中
//            saleAttr.setSpuSaleAttrValueList(saleAttrValueList);
//        }
//        // 返回spu销售的属性集合列表，其中包含属性值
       List<PmsProductSaleAttr> productSaleAttrs=pmsProductSaleAttrDao.spuSaleAttrListCheckBySku(productId,skuId);
        return productSaleAttrs;
    }
}
