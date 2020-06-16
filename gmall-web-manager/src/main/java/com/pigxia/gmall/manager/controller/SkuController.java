package com.pigxia.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.bean.PmsProductInfo;
import com.pigxia.gmall.bean.PmsSkuInfo;
import com.pigxia.gmall.service.SkuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by absen on 2020/5/29 14:22
 */
@Controller
@CrossOrigin
public class SkuController {

    @Reference
    SkuService skuService;

    // 保存一个sku的信息   一个具体的实际商品单元
    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        // 小bug 将前台的skuId存储到spuId
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());

        //  前台如果没有传默认的图片，我们自己设置一个，按理前台应该进行判断
        if(pmsSkuInfo.getSkuDefaultImg()==null){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }
        String result=skuService.saveSkuInfo(pmsSkuInfo);

        return result;
    }
}
