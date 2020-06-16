package com.pigxia.gmall.manager.mapper;

import com.pigxia.gmall.bean.PmsProductSaleAttr;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by absen on 2020/5/29 12:16
 */
public interface PmsProductSaleAttrDao extends Mapper<PmsProductSaleAttr>{
    // 使用Mybatis进行查询
    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(@Param("spuId") String productId,@Param("skuId") String skuId);
}
