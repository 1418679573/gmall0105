package com.pigxia.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.bean.PmsBaseCatalog1;
import com.pigxia.gmall.bean.PmsBaseCatalog2;
import com.pigxia.gmall.bean.PmsBaseCatalog3;
import com.pigxia.gmall.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by absen on 2020/5/27 19:57
 */
@Controller
@CrossOrigin   //  允许跨域的访问注解
public class CatalogController {

    @Reference
    CatalogService catalogService;

    @RequestMapping("/getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){
        List<PmsBaseCatalog1> catalog1s=catalogService.getCatalog1();
        return catalog1s;
    }
    @RequestMapping("/getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(@RequestParam String catalog1Id){
        List<PmsBaseCatalog2> catalog1s=catalogService.getCatalog2(catalog1Id);
        return catalog1s;
    }
    @RequestMapping("/getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(@RequestParam String catalog2Id){
        List<PmsBaseCatalog3> catalog1s=catalogService.getCatalog3(catalog2Id);
        return catalog1s;
    }
}
