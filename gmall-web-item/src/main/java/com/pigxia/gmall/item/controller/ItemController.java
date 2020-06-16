package com.pigxia.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pigxia.gmall.bean.PmsProductSaleAttr;
import com.pigxia.gmall.bean.PmsSkuInfo;
import com.pigxia.gmall.bean.PmsSkuSaleAttrValue;
import com.pigxia.gmall.service.SkuService;
import com.pigxia.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by absen on 2020/5/29 17:56
 */
@Controller
public class ItemController {

    // 根据面向服务的思想，调用skuService服务
    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    //  g根据对应的商品skuid得到对应的商品用于展示
    @GetMapping("{skuId}.html")
    public ModelAndView index(@PathVariable String skuId, ModelAndView model, HttpServletRequest request){
        // 获得skuInfo信息
        String ip=request.getRemoteAddr(); // 获取当前用户的ip地址,进行模拟，得到用户信息
       // String nginx= request.getHeader(""); 此方法使用了nginx做反向代理，得到具体ip地址，用这个方法
        PmsSkuInfo skuInfo = skuService.getSkuInfo(skuId,ip);
        model.addObject("skuInfo", skuInfo).setViewName("item");

        //  查询sku的属性和对应的属性值,根据product_id,和spuId得到对应的当前商品
        List<PmsProductSaleAttr> productSaleAttrList =
                spuService.spuSaleAttrListCheckBySku(skuInfo.getProductId(), skuInfo.getId());
        model.addObject("spuSaleAttrListCheckBySku", productSaleAttrList);

        //  查询出这一sku系列的所有sku集合，进行转化为一个hashMap的json串，放到前台，便于快速切换
         List<PmsSkuInfo> pmsSkuInfoList=skuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());
        Map<String, String> saleAttrValueHashMap = new HashMap<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
             String k="";
             String v=pmsSkuInfo.getId();
          List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
             for (PmsSkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                 k+=skuSaleAttrValue.getSaleAttrValueId()+"|";
             }
            saleAttrValueHashMap.put(k,v);
         }
        String saleAttrValueHashMapJsonStr=JSON.toJSONString(saleAttrValueHashMap);
         model.addObject("saleAttrValueHashMapJsonStr",saleAttrValueHashMapJsonStr);
        return model;
    }


}
