package com.pigxia.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pigxia.gmall.bean.PmsBaseAttrInfo;
import com.pigxia.gmall.bean.PmsBaseAttrValue;
import com.pigxia.gmall.bean.PmsBaseSaleAttr;
import com.pigxia.gmall.manager.mapper.PmsBaseAttrInfoDao;
import com.pigxia.gmall.manager.mapper.PmsBaseAttrValueDao;
import com.pigxia.gmall.manager.mapper.PmsBaseSaleAttrDao;
import com.pigxia.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import tk.mybatis.mapper.entity.Example;

import java.util.HashSet;
import java.util.List;

/**
 * Created by absen on 2020/5/28 10:20
 */
@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoDao pmsBaseAttrInfoDao;

    @Autowired
    PmsBaseAttrValueDao pmsBaseAttrValueDao;

    @Autowired
    PmsBaseSaleAttrDao pmsBaseSaleAttrDao;

    // 查询平台属性
    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo=new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
         List<PmsBaseAttrInfo> pmsBaseAttrInfoList = pmsBaseAttrInfoDao.select(pmsBaseAttrInfo);
//         根据平台属性在查询平台的属性值,加入到平台属性中，用于在sku界面显示
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfoList) {
            PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> pmsBaseAttrValueList = pmsBaseAttrValueDao.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValueList);
        }
        return pmsBaseAttrInfoList;
    }

    // 保存属性和属性值  涉及两张表
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id=pmsBaseAttrInfo.getId();
        if(StringUtils.isBlank(id)){
            //  此时是保存操作
            // 保存平台属性   insertSelective是否将null插入数据库  不插入
            pmsBaseAttrInfoDao.insertSelective(pmsBaseAttrInfo);

            //  保存平台属性值 根据主键返回策略
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue:attrValueList){
                pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                //  将带有平台属性id的属性值插入数据库
                pmsBaseAttrValueDao.insertSelective(pmsBaseAttrValue);
            }
        }else {
            //  id有值 此时是修改操作

            //修改属性  根据属性id来修改属性名称
            Example example=new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",id);
            pmsBaseAttrInfoDao.updateByExampleSelective(pmsBaseAttrInfo,example);

            //  修改属性值
            // 1. 删除对应的属性，在执行保存操作就是修改
            PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(id);
            pmsBaseAttrValueDao.delete(pmsBaseAttrValue);
            
            //  删除后将在保存新的属性值
             List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue baseAttrValue : attrValueList) {
                pmsBaseAttrValueDao.insertSelective(baseAttrValue);
            }

        }


        return "success";
    }

    // 根据属性的名称id得到属性值的集合结果
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue=new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
         List<PmsBaseAttrValue> select = pmsBaseAttrValueDao.select(pmsBaseAttrValue);
        return select;
    }

    //  查询spu的销售属性
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
         List<PmsBaseSaleAttr> pmsBaseSaleAttrs = pmsBaseSaleAttrDao.selectAll();
        return pmsBaseSaleAttrs;
    }

   //  value的id 查询平台属性
    public List<PmsBaseAttrInfo> getAttrNameByVlaueId(HashSet<String> values) {
        String valueIds=StringUtils.join(values,","); //  46,47,48
        List<PmsBaseAttrInfo> pmsBaseAttrInfos=pmsBaseAttrInfoDao.selectAttrNamebyVlaueId(valueIds);
        return pmsBaseAttrInfos;
    }
}
