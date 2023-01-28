// +----------------------------------------------------------------------------
// | 闪速码java短信sdk
// +----------------------------------------------------------------------------
// | Copyright (c) 2014-2021 https://www.shansuma.com
// +----------------------------------------------------------------------------
// | 闪速码短信平台支持网页在线发送、API集成发送、私有化部署三种形式，可免费试用200条
// +----------------------------------------------------------------------------
// | Author: 橘子俊，开发文档请访问 http://sms.shansuma.com/docs
// +----------------------------------------------------------------------------

package com.itheima.reggie.utils.sms;

public class TestClient {

    public static void main(String[] args) {

        Client client = new Client();
        client.setAppId("hw_10902");     //开发者ID，在【设置】-【开发设置】中获取
        client.setSecretKey("bf80b2a52abb94a4fbeb7c61015bf713");    //开发者密钥，在【设置】-【开发设置】中获取
        client.setVersion("1.0");

        /**
         *   json格式可在 bejson.com 进行校验
         */
        String singnstr = "山顶烤肉";
        Client.Request request = new Client.Request();
        request.setMethod("sms.message.send");
        request.setBizContent("{\"mobile\":[\"18616856295\"],\"type\":0,\"template_id\":\"ST_2020101100000003\",\"sign\":\"" + singnstr +"\",\"send_time\":\"\",\"params\":{\"code\":1234}}");  // 这里是json字符串，send_time 为空时可以为null, params 为空时可以为null,短信签名填写审核后的签名本身，不需要填写签名id
        System.out.println( client.execute(request) );
    }

}