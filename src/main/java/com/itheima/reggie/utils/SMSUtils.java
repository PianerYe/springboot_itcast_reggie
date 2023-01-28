package com.itheima.reggie.utils;

import com.itheima.reggie.utils.sms.Client;

public class SMSUtils {

    public static void sendShortMessage(Integer code,String telephone){
        Client client = new Client();
        client.setAppId("hw_11345");     //开发者ID，在【设置】-【开发设置】中获取
        client.setSecretKey("8309e22f4a8d2766d386adfccb1e07cd");    //开发者密钥，在【设置】-【开发设置】中获取
        client.setVersion("1.0");

        String singnstr = "传智健康";
        Client.Request request = new Client.Request();
        request.setMethod("sms.message.send");
//        request.setMethod(method);
//        String code = ValidateCodeUtils.generateValidateCode4String(4);
//        Integer code = ValidateCodeUtils.generateValidateCode(4);
//        System.out.println(code);

        request.setBizContent("{\"mobile\":[\"" +
                ""+ telephone +"\"]," +
                "\"type\":0," +
                "\"template_id\":\"ST_2022083000000005\",\"sign\":\"" + singnstr
                +"\",\"send_time\":\"\",\"params\":{\"code\":\""+ code +"\"}}");
        // 这里是json字符串，send_time 为空时可以为null, params 为空时可以为null,短信签名填写审核后的签名本身，不需要填写签名id


//        System.out.println( client.execute(request) );

    }


}
