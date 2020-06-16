package com.pigxia.gmall.gmallwebelasearch.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.annotations.LoginRequired;
import com.pigxia.gmall.bean.*;
import com.pigxia.gmall.service.AttrService;
import com.pigxia.gmall.service.SearchService;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.ELState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/**
 * Created by absen on 2020/6/3 14:20
 */
@Controller
public class SearchController {
    // 调用elasticSearch 搜索 服务
    @Reference
    SearchService searchService;

    // 调用平台属性的操作服务
    @Reference
    AttrService attrService;

    @LoginRequired(loginSuccess = false)
    @RequestMapping("index")
    public String index(){

        return "index";
    }


    @GetMapping("list.html")
    // 封装查询的参数 平台属性 关键字   三级目录
    public String listItem(PmsSearchParam searchParam, ModelMap map){

        // 查询搜索的商品信息，与el中存储的数据结构一致
        List<PmsSearchSkuInfo> pmsSkuInfoList = searchService.list(searchParam);
        // 商品列表的数据
        map.put("skuLsInfoList", pmsSkuInfoList);

        // 根据vaueId查询平台属性值
        HashSet<String> values = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                values.add(pmsSkuAttrValue.getValueId());
            }
        }
        // 调用attrService查询属性
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrNameByVlaueId(values);
        // 平台属性的数据
        map.put("attrList", pmsBaseAttrInfos);

        // 对已经勾选平台属性，则该属性组需要移除 。此处将平台属性组的去除和面包屑的功能一起做了
        String[] delValueIds = searchParam.getValueId();
        ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        if (delValueIds != null) {
            //  forAttrInfo(pmsBaseAttrInfos, delValueIds); 使用iterator 进行 remove 不会出现并发修改异常 iteratorAttrInfo(pmsBaseAttrInfos, delValueIds);
            for (String delValueId : delValueIds) {
            // 循环的次数代表要面包屑的个数，也是valueId的个数
            PmsSearchCrumb crumb=new PmsSearchCrumb();
            crumb.setValueId(delValueId);
            crumb.setUrlParam(getUrlParam(searchParam,delValueId));
            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
            while (iterator.hasNext()) {
                PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        if (delValueId.equals(pmsBaseAttrValue.getId())) {
                            crumb.setValueName(pmsBaseAttrValue.getValueName());
                            // 删除该属性所在的属性组
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbs.add(crumb);
            }
        }
        // 面包屑的数据
        map.put("attrValueSelectedList",pmsSearchCrumbs);


        // 制作面包屑的功能  PmsSearchCrumb
        // 面包屑的请求=当前请求—要删除的属性的请求  attrValueSelectedList
         //ArrayList<PmsSearchCrumb> pmsSearchCrumbs = getPmsSearchCrumbs(searchParam);

        // 进行当前请求的拼接
        String urlParam = getUrlParam(searchParam);
         // 当前请求即地址栏的数据
        map.put("urlParam", urlParam);

        String keyword=searchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            map.put("keyword", keyword);
        }
        return "list";
    }

    private ArrayList<PmsSearchCrumb> getPmsSearchCrumbs(PmsSearchParam searchParam) {
        String[] delValueId = searchParam.getValueId();
        ArrayList<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
        if (delValueId!=null){
            for (String valueId : delValueId) {
                // 循环的次数代表要面包屑的个数，也是valueId的个数
                PmsSearchCrumb crumb=new PmsSearchCrumb();
                crumb.setValueId(valueId);
                // 此处获取面包屑的名字可以跟删除属性组一起合并，其中包含valueName
                crumb.setValueName(valueId);
                crumb.setUrlParam(getUrlParam(searchParam,valueId));
                pmsSearchCrumbs.add(crumb);
            }
        }
        return pmsSearchCrumbs;
    }


    //  for循环的遍历修改，会报并发修改异常
    private void forAttrInfo(List<PmsBaseAttrInfo> pmsBaseAttrInfos, String[] delValueIds) {
        for (PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfos) {
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                String valueId = pmsBaseAttrValue.getId();
                for (String delValueId : delValueIds) {
                    if (delValueId.equals(valueId)) {
                        pmsBaseAttrInfos.remove(pmsBaseAttrInfo);
                    }
                }
            }
        }
    }
   //  迭代器的遍历 不会报异常
    private void iteratorAttrInfo(List<PmsBaseAttrInfo> pmsBaseAttrInfos, String[] delValueIds) {
        Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
        while (iterator.hasNext()) {
            PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                for (String delValueId : delValueIds) {
                    if (delValueId.equals(pmsBaseAttrValue.getId())) {
                        // 删除该属性所在的属性组
                        iterator.remove();
                    }
                }
            }
        }
    }

    //  请求的地址栏拼接方法 将当前请求和面包屑的请求一起合并了
    public String getUrlParam(PmsSearchParam searchParam,String ...delValueId) {
        String keyword = searchParam.getKeyword();
        String catalog3Id = searchParam.getCatalog3Id();
        String[] skuAttrValueList = searchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam += "keyword=" + keyword;
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam += "catalog3Id=" + catalog3Id;
        }
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if(delValueId.length>0){
                    // 此时走的是面包屑的请求
                    if (!pmsSkuAttrValue.equals(delValueId[0])) {
                        urlParam += "&valueId=" + pmsSkuAttrValue;
                    }

                }else {
                    //  此时走的是当前请求的urlParam
                    urlParam += "&valueId=" + pmsSkuAttrValue;
                }
            }
        }
        return urlParam;
    }

    // 面包屑的地址拼接 方法
    public String getUrlParamForCrumb(PmsSearchParam searchParam,String delValueId) {
        String keyword = searchParam.getKeyword();
        String catalog3Id = searchParam.getCatalog3Id();
        String[] skuAttrValueList = searchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam += "keyword=" + keyword;
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam += "&";
            }
            urlParam += "catalog3Id=" + catalog3Id;
        }
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {


            }
        }
        return urlParam;
    }
}
