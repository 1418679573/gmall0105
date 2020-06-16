package com.pigxia.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.bean.PmsBaseAttrInfo;
import com.pigxia.gmall.bean.PmsBaseAttrValue;
import com.pigxia.gmall.bean.PmsBaseSaleAttr;
import com.pigxia.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by absen on 2020/5/28 10:12
 */
@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

     // 查询平台属性
    @RequestMapping("/attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(@RequestParam String catalog3Id){
        List<PmsBaseAttrInfo> infoLists=attrService.attrInfoList(catalog3Id);
        return infoLists;
    }

    //  保存平台属性
    @RequestMapping("/saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        String success=attrService.saveAttrInfo(pmsBaseAttrInfo);
        return success;
    }



    //  保存平台属性
    @RequestMapping("/getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(@RequestParam String attrId){
       List<PmsBaseAttrValue> pmsBaseAttrValues=attrService.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }

    //  查询spu的基本属性  baseSaleAttrList
    @RequestMapping("/baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){
        List<PmsBaseSaleAttr> pmsBaseAttrValues=attrService.baseSaleAttrList();
        return pmsBaseAttrValues;
    }

}
