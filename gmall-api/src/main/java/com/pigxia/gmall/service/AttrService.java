package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.PmsBaseAttrInfo;
import com.pigxia.gmall.bean.PmsBaseAttrValue;
import com.pigxia.gmall.bean.PmsBaseSaleAttr;

import java.util.HashSet;
import java.util.List;

/**
 * Created by absen on 2020/5/28 10:17
 */
public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    List<PmsBaseAttrInfo> getAttrNameByVlaueId(HashSet<String> values);
}
