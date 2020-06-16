package com.pigxia.gmall.cart.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pigxia.gmall.annotations.LoginRequired;
import com.pigxia.gmall.bean.OmsCartItem;
import com.pigxia.gmall.bean.PmsSkuInfo;
import com.pigxia.gmall.service.CartService;
import com.pigxia.gmall.service.SkuService;
import com.pigxia.gmall.utils.CookieUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by absen on 2020/6/4 15:52
 */
@Controller
public class CartController {


    @Reference
    SkuService skuService;

    @Reference
    CartService cartService;


    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public String addToCart(String skuId, BigDecimal quantity, HttpServletRequest request, HttpServletResponse response){

        // 调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuInfo(skuId,"");

        //将商品信息封装到购物车信息

        OmsCartItem cartItem=new OmsCartItem();

        cartItem.setCreateDate(new Date());
        cartItem.setDeleteStatus(0);
        cartItem.setModifyDate(new Date());
        cartItem.setPrice(skuInfo.getPrice());
        cartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        cartItem.setProductId(skuInfo.getProductId());
        cartItem.setProductSkuId(skuId);
        cartItem.setProductName(skuInfo.getSkuName());
        cartItem.setProductPic(skuInfo.getSkuDefaultImg());
        cartItem.setQuantity(quantity);
        cartItem.setIsChecked("1");
        cartItem.setProductSkuCode("111111111");


        //判断用户是否登录
        String memberId = request.getParameter("memberId");

             //根据用户登录是否存cookie和数据库

        if (StringUtils.isNotBlank(memberId)) {
            // 用户已经登录，从db中查询出购物车数据
            OmsCartItem cartItemFromDb=cartService.ifCartExitsByUser(memberId,skuId);
             //  如果购物车为null
            if(cartItemFromDb==null){
                // 直接插入数据
                cartItem.setMemberId(memberId);
                cartService.addCart(cartItem);
            }else {
                // 用户添加过该商品，对其进行更新操作
                cartItemFromDb.setQuantity(cartItemFromDb.getQuantity().add(cartItem.getQuantity()));
               cartService.updateCart(cartItemFromDb);
            }
            //  同步到缓存中
            cartService.flushCache(memberId);

        } else {
            // 未登录，走cookie,跨域问题
            List<OmsCartItem> omsCartItems = new ArrayList<>();
            //   取出cookie中的数据
            String cookieList = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if (StringUtils.isBlank(cookieList)) {
                // cookie为空
                omsCartItems.add(cartItem);
            } else {
                //cookie不为空
                omsCartItems = JSONArray.parseArray(cookieList, OmsCartItem.class);
                // 对数据进行更新操作后在存入cookie
                boolean exits = if_exit_cartSkuInfo(omsCartItems, cartItem);
                if (exits) {
                    //之前添加过，更新购物车数量
                    for (OmsCartItem omsCartItem : omsCartItems) {
                         if (omsCartItem.equals(cartItem)){
                             // 对数量和价格进行更新
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                            cartItem.setPrice(cartItem.getPrice().add(omsCartItem.getPrice()));
                         }
                    }
                } else {
                    // 之前没有添加过,新增当前的购物车
                    omsCartItems.add(cartItem);
                }
            }
            // 更新Cookie
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);
        }

        return "redirect:/success.html";
    }

    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false)
    public String cartList(HttpServletRequest request, ModelMap map){
        List<OmsCartItem> omsCartItems;
        // 判断用户的登录状态来进行查询的是cookie还是db
        String memberId = (String) request.getAttribute("memberId");
        if(StringUtils.isNotBlank(memberId)){
            // 查询db中的数据
            omsCartItems=cartService.cartList(memberId);
        }else {
            // 查询的是缓存中的数据
            String cookieList = CookieUtil.getCookieValue(request, "cartListCookie", true);
            omsCartItems = JSON.parseArray(cookieList, OmsCartItem.class);
        }
        // 计算所有的商品的总价格
        BigDecimal totalAccountPrice=totalAccountPrice(omsCartItems);
        map.put("totalAccountPrice",totalAccountPrice);
        map.put("cartList",omsCartItems);
        return "cartList";
    }



    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked,String skuId,HttpServletRequest request, ModelMap map){

        // 用户登录了才能使用勾选功能来更改数据库
        String memberId = (String) request.getAttribute("memberId");
        OmsCartItem omsCartItem=new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setIsChecked(isChecked);
        omsCartItem.setProductSkuId(skuId);

        // 调用修改功能
        cartService.checkCart(omsCartItem);
        //  查询购物车清单
         List<OmsCartItem> omsCartItems = cartService.cartList(memberId);
        // 通过前台异步请求，返回一个内嵌页面展示
        map.put("cartList",omsCartItems);
        // 计算所有的商品的总价格
        BigDecimal totalAccountPrice=totalAccountPrice(omsCartItems);
        map.put("totalAccountPrice",totalAccountPrice);
        return "cartListInner";
    }

    private boolean if_exit_cartSkuInfo(List<OmsCartItem> omsCartItems, OmsCartItem cartItem) {
        boolean b=false;
        for (OmsCartItem omsCartItem : omsCartItems) {
            if (cartItem.getProductId().equals(omsCartItem.getProductId())){
                b=true;
            }
        }
        return b;
    }
    private BigDecimal totalAccountPrice(List<OmsCartItem> omsCartItems) {
        // 使用字符串进行初始化，可以避免浮点 单精度的丢失
        BigDecimal price=new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                price=price.add(omsCartItem.getTotalPrice());
            }
        }
        return price;
    }

}
