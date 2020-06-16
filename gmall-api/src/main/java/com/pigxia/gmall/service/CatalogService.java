package com.pigxia.gmall.service;

import com.pigxia.gmall.bean.PmsBaseCatalog1;
import com.pigxia.gmall.bean.PmsBaseCatalog2;
import com.pigxia.gmall.bean.PmsBaseCatalog3;

import java.util.List;

/**
 * Created by absen on 2020/5/27 20:27
 */
public interface CatalogService {
    List<PmsBaseCatalog1> getCatalog1();

    List<PmsBaseCatalog2> getCatalog2(String catalog1Id);

    List<PmsBaseCatalog3> getCatalog3(String catalog2Id);
}
