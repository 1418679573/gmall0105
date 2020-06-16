package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by absen on 2020/5/29 14:26
 */
public interface SkuService { ;

    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuInfo(String skuId,String ip);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAll(String s);

    boolean checkPrice(String productSkuId, BigDecimal price);
}
