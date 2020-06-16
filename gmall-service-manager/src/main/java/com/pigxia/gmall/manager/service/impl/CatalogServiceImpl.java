package com.pigxia.gmall.manager.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pigxia.gmall.bean.PmsBaseCatalog1;
import com.pigxia.gmall.bean.PmsBaseCatalog2;
import com.pigxia.gmall.bean.PmsBaseCatalog3;
import com.pigxia.gmall.manager.mapper.PmsBaseCatalog1Dao;
import com.pigxia.gmall.manager.mapper.PmsBaseCatalog2Dao;
import com.pigxia.gmall.manager.mapper.PmsBaseCatalog3Dao;
import com.pigxia.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by absen on 2020/5/27 20:29
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    @Autowired
    PmsBaseCatalog1Dao pmsBaseCatalog1Dao;

    @Autowired
    PmsBaseCatalog2Dao pmsBaseCatalog2Dao;

    @Autowired
    PmsBaseCatalog3Dao pmsBaseCatalog3Dao;
    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Dao.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2s=new PmsBaseCatalog2();
        pmsBaseCatalog2s.setCatalog1Id(catalog1Id);

        return  pmsBaseCatalog2Dao.select(pmsBaseCatalog2s);
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3s=new PmsBaseCatalog3();
        pmsBaseCatalog3s.setCatalog1Id(catalog2Id);

        return  pmsBaseCatalog3Dao.select(pmsBaseCatalog3s);
    }
}
