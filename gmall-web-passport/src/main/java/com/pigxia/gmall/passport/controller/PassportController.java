package com.pigxia.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pigxia.gmall.annotations.LoginRequired;
import com.pigxia.gmall.bean.UmsMember;
import com.pigxia.gmall.service.UserService;

import com.pigxia.gmall.util.HttpclientUtil;
import com.pigxia.gmall.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by absen on 2020/6/5 10:48
 */
@Controller
public class PassportController {

    @Reference
    UserService userService;


    @RequestMapping("vlogin")
    public String vlogin(String code,HttpServletRequest request) {

        //  将获取到的code去换取access_token
        String s3="https://api.weibo.com/oauth2/access_token";
        Map<String, String> map = new HashMap<>();
        map.put("client_id","1561099830");
        map.put("client_secret","9728d4fb373ae62091535f5732b6a05b");
        map.put("grant_type","authorization_code");
        map.put("redirect_uri","http://passport.gmall.com:8070/vlogin");
        map.put("code",code);
        String access_token_json= HttpclientUtil.doPost(s3,map);
        Map user_token_info = JSON.parseObject(access_token_json, Map.class);
        //  通过access_token 去换取用户信息
        String access_token= (String) user_token_info.get("access_token");
        String uid= (String) user_token_info.get("uid");
        String s4="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String access_info_json= HttpclientUtil.doGet(s4);
        Map user_info = JSON.parseObject(access_info_json, Map.class);
        // 将用户信息保存到数据库，用户类型设置为微博用户（本次调用的是微博接口服务器）
         UmsMember umsMember = new UmsMember();
         umsMember.setMemberLevelId((String) user_info.get("idstr"));
         umsMember.setNickname((String) user_info.get("screen_name"));
         //  1表示本网站的用户登录 2 微博
         umsMember.setSourceType("2");
         umsMember.setSourceUid(uid);
         umsMember.setAccessCode(code);
         umsMember.setAccessToken(access_token);
         UmsMember umsMemberCheck = new UmsMember();
         umsMemberCheck.setSourceUid(uid);
         UmsMember umsMemberExist=userService.UmsMemberCheck(umsMemberCheck);
         if (umsMemberExist==null){
             // rpc远程调用主键返回策略会失效
             umsMember= userService.insertOauth2(umsMember);
         }
        // 用jwt生成新的token，重定向首页，携带该token
        // 用户存在
        // jwt生成一份token  1.
        //  通过nginx转发的客户端ip
        String ip="";
        String token="";
        ip= request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip)){
            ip=request.getRemoteAddr();
            if (StringUtils.isBlank(ip)){
                ip="127.0.0.1";
            }
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("memberId",umsMember.getId()); // 使用dubbo的rpc远程调用主键返回策略会失效
        userMap.put("nickName",umsMember.getNickname());
        // 制作jwt的token  1.公钥 2.用户私人信息  3.盐值 用来加密，数字签名
        token=JwtUtil.encode("gmall-service",userMap,ip+"");
        // 存入一份token到redis中
        userService.addUserTokenCache(token,umsMember.getMemberLevelId());
        return "redirect:http://search.gmall.com:8071/index?token="+token;
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request){
        String ip="";
        String token="";
        // 进行登录校验
        UmsMember user=userService.login(umsMember);
        if (user!=null){
            // 用户存在
            // jwt生成一份token  1.
            //  通过nginx转发的客户端ip
           ip= request.getHeader("x-forwarded-for");
           if (StringUtils.isBlank(ip)){
               ip=request.getRemoteAddr();
               if (StringUtils.isBlank(ip)){
                   ip="127.0.0.1";
               }
           }
             Map<String, Object> userMap = new HashMap<>();
           userMap.put("memberId",user.getMemberLevelId());
           userMap.put("nickName",user.getNickname());
           // 制作jwt的token  1.公钥 2.用户私人信息  3.盐值 用来加密，数字签名
            token=JwtUtil.encode("gmall-service",userMap,ip+"");
            // 存入一份token到redis中
            userService.addUserTokenCache(token,user.getMemberLevelId());
        }else {
            // 用户不存在
            token="fail";
        }


        return token;
    }
    // 直接从首页点击登录来到前台也要拦截校验登录也要设置
    @LoginRequired(loginSuccess = false)
    @RequestMapping("index")
    public String index(String returnUrl, ModelMap map){
        map.put("returnUrl",returnUrl);
        return "index";
    }
    // 认证页面
    @RequestMapping("authentication")
    @ResponseBody
    public String authentication(String token,String currentIp){
        // currentIp 是当前请求的ip，从登录方法到认证方法经过了两次请求，为了确保是同一个ip，在拦截认证方法时传入当前的ip
         HashMap<String, Object> successMap = new HashMap<>();
        // 对token进行校验
         Map<String, Object> decode = JwtUtil.decode(token, "gmall-service", currentIp);
         if (decode!=null){
             // 解密成功
             successMap.put("status","success");
             successMap.put("memberId",decode.get("memberId"));
             successMap.put("nickName",decode.get("nickName"));
         }else {
             successMap.put("status","fail");
         }
        return JSON.toJSONString(successMap);
    }
}
