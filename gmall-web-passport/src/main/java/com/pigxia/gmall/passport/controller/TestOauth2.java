package com.pigxia.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.pigxia.gmall.util.HttpclientUtil;
import com.sun.deploy.net.HttpUtils;
import org.apache.http.client.utils.HttpClientUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by absen on 2020/6/8 16:03
 */
public class TestOauth2 {
    public static void main(String[] args) {

      /*  1. 引导用户到如下地址
         https://api.weibo.com/oauth2/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI
       */

        //  1561099830
        //  http://passport.gmall.com:8070/vlogin
       String url1= HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=1561099830&response_type=code&redirect_uri=http://passport.gmall.com:8070/vlogin");

       // 2.获得回调地址，带了encode授权码
       //  assport.gmall.com/vlogin?code=3cd611d9c4b805a8ddda324b685fa2c7
       String url2=url1;


       //  3. 使用code去交换到access_token

        String s3="https://api.weibo.com/oauth2/access_token";
         Map<String, String> map = new HashMap<>();
        map.put("client_id","1561099830");
        map.put("client_secret","9728d4fb373ae62091535f5732b6a05b");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://passport.gmall.com:8070/vlogin");
        map.put("code","code");

        String access_token_json=HttpclientUtil.doPost(s3,map);
        Map user_token_info = JSON.parseObject(access_token_json, Map.class);

         //  4.使用acccess_token得到用户的信息

        String s4="https://api.weibo.com/2/users/show.json";
        HashMap<String, String> stringStringHashMap = new HashMap<>();


    }
}
