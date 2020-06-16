package com.pigxia.gmall.manager.mapper;

import com.pigxia.gmall.bean.PmsSkuInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by absen on 2020/5/29 14:48
 */
public interface PmsSkuInfoDao extends Mapper<PmsSkuInfo> {
    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(String productId);
}
