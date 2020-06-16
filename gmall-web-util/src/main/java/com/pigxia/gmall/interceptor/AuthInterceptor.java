package com.pigxia.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pigxia.gmall.annotations.LoginRequired;
import com.pigxia.gmall.util.HttpclientUtil;
import com.pigxia.gmall.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


    public AuthInterceptor() {
        super();
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//    String newToken = request.getParameter("newToken");
//    if(newToken!=null&&newToken.length()>0){
//        CookieUtil.setCookie(request,response,"token",newToken,WebConst.cookieExpire,false);
//    }
    HandlerMethod handlerMethod=(HandlerMethod)handler;
     LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
     StringBuffer url=request.getRequestURL();
    if (loginRequired==null){
        //  方法上没有添加该注解，则表示该请求不需要认证，直接放行
        return true;
    }
    String token="";
    String oldToken=CookieUtil.getCookieValue(request,"oldCookie",true);
    if(StringUtils.isNotBlank(oldToken)){
        token=oldToken;
    }
    String newToken=request.getParameter("token");
    if(StringUtils.isNotBlank(newToken)){
        token=newToken;
    }
    boolean loginSuccess=loginRequired.loginSuccess(); //
    String success="fail";
        Map <String,Object> successMap=new HashMap<>();
        String ip="";
        if(StringUtils.isNotBlank(token)){
            //  通过nginx转发的客户端ip 从登录到认证方法之间有两次请求，确保同一次请求，传入当前ip
            ip= request.getHeader("x-forwarded-for");
            if (StringUtils.isBlank(ip)){
                ip=request.getRemoteAddr();
                if (StringUtils.isBlank(ip)){
                    ip="127.0.0.1";
                }
            }
        //  调用认证中心去进行认证   authentication
            String mapString = HttpclientUtil.doGet("http://passport.gmall.com:8070/authentication?token=" + token+"&currentIp="+ip);
             successMap = JSON.parseObject(mapString);
             success=(String) successMap.get("status");
        }
    if (loginSuccess){
           // 表示    true 代表一定需要认证才能访问（订单列表）
          if(!success.equals("success")){
              //  认证不通过，则直接跳到登录页面，但是要保留当前请求的地址，方便登录成功，直接返回到该界面
              StringBuffer returnUrl=request.getRequestURL();
              response.sendRedirect("passport.gmall.com:8070/index?returnUrl="+returnUrl);
              return false;
          }
          // 认证成功
        request.setAttribute("memberId",successMap.get("memberId"));
        request.setAttribute("nickName",successMap.get("nickName"));
        CookieUtil.setCookie(request,response,"oldCookie",token,60*60*2,true);
    }else {
        //false不一定认证也可以访问  购物车列表
       if(success.equals("success")){
           //  认证成功
           request.setAttribute("memberId",successMap.get("memberId"));
           request.setAttribute("nickName",successMap.get("nickName"));
        CookieUtil.setCookie(request,response,"oldCookie",token,60*60*2,true);
       }
       // 认证失败也可以访问当前的页面
    }
    return true;
}

}