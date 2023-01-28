package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import com.itheima.reggie.utils.sms.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@Slf4j
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        log.info("user: {}",user);
        //先判断电话号码不为空
        if (user.getPhone() == null){
            return R.error("手机号不能为空");
        }
        //判断手机号是否在数据库中，不存在则要求客户先注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone,user.getPhone());
        int count = userService.count(queryWrapper);
        if (count == 0){
            return R.error("手机号不存在，请申请注册");
        }

        //随机生成一个6位数字验证码
        String code = ValidateCodeUtils.generateValidateCode(6).toString();
        //给用户发送验证码
        SMSUtils.sendShortMessage(Integer.parseInt(code), user.getPhone());
        //将生成的验证码保存到Session中
        session.setAttribute(user.getPhone(),code);
        return R.success("手机验证码发送成功");
    }
}
