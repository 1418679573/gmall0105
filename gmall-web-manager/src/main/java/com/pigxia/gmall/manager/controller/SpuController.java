package com.pigxia.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.bean.PmsProductImage;
import com.pigxia.gmall.bean.PmsProductInfo;
import com.pigxia.gmall.bean.PmsProductSaleAttr;
import com.pigxia.gmall.manager.util.PmsUploadImage;
import com.pigxia.gmall.service.SpuService;
import org.csource.common.MyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Created by absen on 2020/5/28 13:26
 */
@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(@RequestParam String catalog3Id){
        List<PmsProductInfo> pmsProductInfos=spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    //  获取图片信息
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
       // 将图片存储到分布式文件存储系统
        // 将图片的存储路径返回给页面
        String imageUrl= PmsUploadImage.uploadImage(file);
        System.out.println(imageUrl);
        return imageUrl;
    }

    // 保存一个spu的信息  standard product unit 标准的用户单元
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
       String result=spuService.saveSpuInfo(pmsProductInfo);
       return result;
    }

    // 查询出销售属性和销售属性值，根据对应spuid
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(@RequestParam String spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrList=spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrList;
    }


    // 查询出图片信息显示，根据对应spuid
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(@RequestParam String spuId){
        List<PmsProductImage> pmsProductImageList=spuService.spuImageList(spuId);
        return pmsProductImageList;
    }
}


