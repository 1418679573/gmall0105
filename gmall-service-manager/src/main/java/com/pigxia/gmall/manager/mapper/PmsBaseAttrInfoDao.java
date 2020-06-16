package com.pigxia.gmall.manager.mapper;

import com.pigxia.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by absen on 2020/5/28 10:21
 */
public interface PmsBaseAttrInfoDao extends Mapper<PmsBaseAttrInfo>{
    List<PmsBaseAttrInfo> selectAttrNamebyVlaueId(@Param("valueIds") String valueIds);
}
