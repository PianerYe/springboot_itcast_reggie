package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.itheima.reggie.utils.SMSUtils.*;

@RestController
@Slf4j
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 发送手机短信验证码
     * */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user){//, HttpSession session){
        log.info("user: {}",user);
        //先判断电话号码不为空
        if (user.getPhone() == null){
            return R.error("手机号不能为空");
        }

        //随机生成一个6位数字验证码
        String code = ValidateCodeUtils.generateValidateCode(6).toString();
        //给用户发送验证码
        System.out.println(code);
//        sendShortMessage(Integer.parseInt(code), user.getPhone());  //为了省钱，不再发了
        //将生成的验证码保存到Session中
//        session.setAttribute(user.getPhone(),code);

        //将生成的验证码缓存到Redis中，并且设置有效期为五分钟
        redisTemplate.opsForValue().set(user.getPhone(),code,5,TimeUnit.MINUTES);

        return R.success("手机验证码发送成功");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){

        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();
        //从session获取保存的验证码
        //Object codeSession = session.getAttribute(phone);

        //从Redis中获取缓存的验证码
        Object codeSession = redisTemplate.opsForValue().get(phone);

        if (codeSession != null && codeSession.equals(code)){
            //进行验证码的比对，说明登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user == null){
                //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            //如果用户登录成功，删除redis中缓存的验证码
            redisTemplate.delete(user.getPhone());

            return R.success(user);

        }
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> login(HttpServletRequest request){
        //清理session中保存的当前登录的用户ID
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }

    @GetMapping("/userInfo")
    public R<User> userInfo(HttpSession session){
        User userInfo = new User();
        log.info("session.user:{}",session.getAttribute("user"));
        User user = userService.getById((Serializable) session.getAttribute("user"));
        if (user.getName() == null && user.getName() == ""){
            userInfo.setName(user.getPhone());
        }else {
            userInfo.setName(user.getName());
        }
        userInfo.setSex(user.getSex());
        log.info("!!!!!userInfo:{}",userInfo);
        return R.success(userInfo);
    }
}
