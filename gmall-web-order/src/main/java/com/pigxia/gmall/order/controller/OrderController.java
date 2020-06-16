package com.pigxia.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pigxia.gmall.annotations.LoginRequired;
import com.pigxia.gmall.bean.OmsCartItem;
import com.pigxia.gmall.bean.OmsOrder;
import com.pigxia.gmall.bean.OmsOrderItem;
import com.pigxia.gmall.bean.UmsMemberReceiveAddress;
import com.pigxia.gmall.service.CartService;
import com.pigxia.gmall.service.OrderService;
import com.pigxia.gmall.service.SkuService;
import com.pigxia.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by absen on 2020/6/8 20:12
 */
@Controller
public class OrderController {

    @Reference
    UserService userService;

    @Reference
    CartService cartService;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;

    @RequestMapping("submitOrder")
    // 自定义注解  需要通过认证中心认证，true表示一定要认证通过才能访问的页面
    @LoginRequired(loginSuccess = true)
    public String submitOrder(String deliveryAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, ModelMap map) {
        String memberId = (String) request.getAttribute("memberId");
        //  检验交易码，防止用户重复提交订单
        String success = orderService.checkTradeCode(memberId, tradeCode);
        if (success.equals("success")) {
            ArrayList<OmsOrderItem> omsOrderItems = new ArrayList<>();
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);//自动收货时间
            omsOrder.setCreateTime(new Date());// 订单创建时间
            omsOrder.setDiscountAmount(null);// 折扣价
            omsOrder.setMemberId(memberId);
            omsOrder.setNote("快点发货");// 备注信息
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + dateFormat.format(new Date());
            omsOrder.setOrderSn(outTradeNo);// 订单号
            omsOrder.setPayAmount(totalAmount);
            omsOrder.setOrderType(1);//订单类型
            UmsMemberReceiveAddress address = userService.getReceiveAddressById(deliveryAddressId);
            omsOrder.setReceiverProvince(address.getProvince());
            omsOrder.setReceiverCity(address.getCity());
            omsOrder.setReceiverDetailAddress(address.getDetailAddress());
            omsOrder.setReceiverName(address.getName());
            omsOrder.setReceiverPhone(address.getPhoneNumber());
            omsOrder.setReceiverPostCode(address.getPostCode());
            omsOrder.setReceiverRegion(address.getRegion());
            // 当前日期加一天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            Date d = calendar.getTime();
            omsOrder.setReceiveTime(d);
            omsOrder.setTotalAmount(totalAmount);
            omsOrder.setStatus("0");// 订单状态 0 未付款 1未发货 2. 已发货
            omsOrder.setSourceType(0); // 订单来源 pc app
            // 交易码核对成功存在
            //  进行对购物车的商品列表进行写的操作，即删除购物车中的数据
            // 1. 根据memberId获取到购物车中的数据和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")) {
                    // 获取订单的详情列表

                    // 检验价格 将详情中的价格和skuInfo中的价格进行比对，可能价格出现了变化
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(), omsCartItem.getPrice());
                    if (!b) {
                        return "tradeFail";
                    }
                    //2. 验库存，远程调用库存系统
                    OmsOrderItem omsOrderItem = new OmsOrderItem();
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setRealAmount(omsCartItem.getTotalPrice());
                    omsOrderItem.setOrderSn(outTradeNo);// 订单号，用来调用第三方支付接口
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductSkuCode("1111111111");// 条形码
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductSn("仓库中的productId");


                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOrderItemList(omsOrderItems);
            //3.将订单和订单详情写入数据库
            orderService.saveOrder(omsOrder);
            //4.调用支付服务
            // 重定向到支付页面
            return "redirect:http://payment.gmall.com:8076/index";

        } else {

            return "tradeFail";
        }
    }

    @RequestMapping("toTrade")
    // 自定义注解  需要通过认证中心认证，true表示一定要认证通过才能访问的页面
    @LoginRequired(loginSuccess = true)
    public String toTrade(HttpServletRequest request, ModelMap map) {

        String memberId = (String) request.getAttribute("memberId");

        // 查询收货人的地址
        List<UmsMemberReceiveAddress> receiveAddress = userService.getReceiveAddressByMemberId(memberId);
        map.put("userAddressList", receiveAddress);


        // 根据memberId 查看购物车列表的数据封装成订单数据
        ArrayList<OmsOrderItem> omsOrderItems = new ArrayList<>();
        List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }
        }
        //  根据用户的di生成一份交易码，防止用户提交订单后回退到结算页面重复提交
        String tradeCode = orderService.genTradeCode(memberId);
        map.put("tradeCode", tradeCode);
        map.put("totalAmount", totalAccountPrice(omsCartItems));
        map.put("omsOrderItems", omsOrderItems);
        return "trade";
    }

    private BigDecimal totalAccountPrice(List<OmsCartItem> omsCartItems) {
        // 使用字符串进行初始化，可以避免浮点 单精度的丢失
        BigDecimal price = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (omsCartItem.getIsChecked().equals("1")) {
                price = price.add(omsCartItem.getTotalPrice());
            }
        }
        return price;
    }

}

