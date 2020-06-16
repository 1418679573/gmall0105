package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.PmsProductImage;
import com.pigxia.gmall.bean.PmsProductInfo;
import com.pigxia.gmall.bean.PmsProductSaleAttr;

import java.util.List;

/**
 * Created by absen on 2020/5/28 13:28
 */
public interface SpuService {
    List<PmsProductInfo> spuList(String catalog3Id);


    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);
}
