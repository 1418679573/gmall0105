package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.PmsSearchParam;
import com.pigxia.gmall.bean.PmsSearchSkuInfo;

import java.util.List;


/**
 * Created by absen on 2020/6/3 15:24
 */
public interface SearchService {

    List<PmsSearchSkuInfo> list(PmsSearchParam searchParam);
}
