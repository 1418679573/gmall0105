# _**`gmall0105`**_

gmall-web-passport  8070  用户认证中心前台
gmall-service-user  8060 调用用户的服务查询数据库

gmall-web-search  8071
gmall-service-search  8061

gmall-web-manager 用来接收前端页面的数据  作为服务消费方  8072
gmall-service-manager  用来从数据库中获取我们需要的数据  作为服务提供方 8062

gmall-web-item  商品的详情页服务 端口号 8073

gmall-web-cart   8074   购物车服务
gmall-service-cart 8064


gmall-web-order 8075  订单服务
gmall-service-order 8065

gmall-payment 8076 支付服务


CatalogController 用来查询三级的目录的数据 

AttrController  在三级目录下用来做属性名称和属性值的CRUD操作

SpuController  在三级目录下用来查询相应的商品模板信息（）  




