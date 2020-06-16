package com.pigxia.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.pigxia.gmall.annotations.LoginRequired;
import com.pigxia.gmall.bean.OmsOrder;
import com.pigxia.gmall.bean.PaymentInfo;
import com.pigxia.gmall.payment.controller.config.AlipayConfig;
import com.pigxia.gmall.payment.service.PaymentService;
import com.pigxia.gmall.service.OrderService;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.CHARSET;

/**
 * Created by absen on 2020/6/10 12:59
 */
@Controller
public class PaymentController {

    @Reference
    OrderService orderService;

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;


    @PostMapping("alipay/callback/return")
    @LoginRequired()
    public String alipayCallback(HttpServletRequest request, ModelMap map){

        // 更新用户的支付状态
        String sign=request.getParameter("sign");// 获取数字签名用来验签
        String trade_no=request.getParameter("trade_no");// 支付宝交易凭证号
        String out_trade_no=request.getParameter("out_trade_no");// 商品的订单号
        String trade_status=request.getParameter("trade_status"); // 交易状态
        String total_amount=request.getParameter("total_amount");// 订单总额
        String subject=request.getParameter("subject"); //商品标题
        String callbackContent=request.getQueryString(); // 请求的所有参数

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus("已付款");
        paymentInfo.setCallbackContent(callbackContent);
        paymentInfo.setAlipayTradeNo(trade_no);
        paymentInfo.setCallbackTime(new Date());
        // 通过支付宝的paramsMap进行签名验证，2.0版本接口将该paramsMap去掉了，导致同步请求没法验签
        if (StringUtils.isNotBlank(sign)){
            // 拿着支付宝的公钥进行对数字签名验签
            // 接下来 订单服务==》库存服务==》物流服务
            // 模拟验签成功,更新支付消息为已支付，同时通过activemq去并发一个订单服务
            paymentService.updatePayment(paymentInfo);// 此时内部需要进行幂等性的检查
        }


        return "finish";
    }


    @PostMapping("mx/submit")
    @LoginRequired()
    public String mx(HttpServletRequest request, ModelMap map){

        return null;
    }

    @PostMapping("alipay/submit")
    @LoginRequired()
    @ResponseBody
    public String aplipay(String outTradeNo, String totalAmount, HttpServletRequest request, HttpServletResponse response, ModelMap map) throws IOException {
        AlipayTradePagePayRequest alipayRequest =  new  AlipayTradePagePayRequest(); //创建API对应的request
         // 回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String,Object> param=new HashMap<>();
        param.put("out_trade_no",outTradeNo);
        param.put("product_code","FAST_INSTANT_TRADE_PAY");
        param.put("total_amount",0.01);
        param.put("subject","华为p9超感光系列6G+128G");
        String content= JSON.toJSONString(param);
        alipayRequest.setBizContent(content);
        // 获得支付宝的一个请求的客户端（它并不是一个链接，而是一个封装好的http的表单请求）
        String form= "" ;
        try  {
            form = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
        }  catch  (AlipayApiException e) {
            e.printStackTrace();
        }
        //保存用户的支付信息
        OmsOrder order=orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setOrderSn(order.getOrderSn());
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setTotalAmount(order.getTotalAmount());
        paymentInfo.setSubject(null);
        paymentInfo.setPaymentStatus("商品标题！");
        paymentService.savePaymentInfo(paymentInfo);

        // 在支付完成之前，因为支付成功支付宝才会返回我们的回调地址
        //因此可以自定义一个延迟队列，自己去询问支付状态，由被动变为主动
        paymentService.sendDelayPaymentResult(outTradeNo,5);

        return  form;
    }
    @GetMapping("index")
    @LoginRequired()
    public String index(HttpServletRequest request, ModelMap map){
        String memberId= (String) request.getAttribute("memberId");
        OmsOrder order =orderService.getOrderByMemberId(memberId);
        map.put("outTradeNo",order.getOrderSn());
        map.put("totalAmount",order.getTotalAmount());
        return "index";
    }
}
