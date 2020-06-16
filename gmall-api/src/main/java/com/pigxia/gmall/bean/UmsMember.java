package com.pigxia.gmall.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by absen on 2020/5/27 9:52
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UmsMember implements Serializable {
    // 配置通用mapper的主键和主键返回策略
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String         memberLevelId;
    private String username;
    private String         password;
    private String nickname;
    private String         phone;
    private int status;
    private Date createTime;
    private String icon;
    private int         gender;
    private Date birthday;
    private String   city;
    private String job;
    private String  personalizedSignature;
    private String sourceType;
    private int         integration;
    private int growth;
    private int luckeyCount;
    private int historyIntegration;
    private String sourceUid;
    private String accessCode;
    private String accessToken;
}
