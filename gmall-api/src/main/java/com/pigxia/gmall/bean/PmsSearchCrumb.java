package com.pigxia.gmall.bean;

/**
 * Created by absen on 2020/6/4 13:12
 */
public class PmsSearchCrumb {

    private String urlParam; // 面包屑的当前请求
    private String valueId;
    private String valueName;

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }

    public String getValueId() {
        return valueId;
    }

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
}
