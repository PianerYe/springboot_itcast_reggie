package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("employee")
@Api(tags = "员工相关接口")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录接口")
    @ApiImplicitParam(name = "employee",value = "员工登录用户名",required = true)
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据页面提交的用户名username查数据库
        LambdaQueryWrapper<Employee> queryWarpper = new LambdaQueryWrapper<>();
        queryWarpper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWarpper);

        //3.如果没有查询到则返回登录失败结果
        if (emp == null){
            return R.error("登录失败");
        }

        //4.密码比对，如果不一致则返回登陆失败结果
        if (!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //5.查看员工状态，如果为禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0){
            return R.error("账号已禁用");
        }
        //6.登录成功，将员工ID写入session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }
    /**
     * 员工退出
     * */
    @PostMapping("/logout")
    @ApiOperation(value = "员工退出接口")
    public R<String> logout(HttpServletRequest request){
        //清理session中保存的当前登录员工ID
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * */
    @PostMapping
    @ApiOperation(value = "新增员工接口")
    @ApiImplicitParam(name = "employee",value = "员工信息",required = true)
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息: {}",employee.toString());

        //设置初始密码123456，需要进行MD5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());

        //获得当前登录用户ID
//        Long empId = (Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);

        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * */
    @GetMapping("/page")
    @ApiOperation(value = "员工分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "员工姓名",required = false)
    })
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);//模糊查询，利用like
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     *  修改员工信息
    */
    @PutMapping
    @ApiOperation(value = "修改员工信息接口")
    @ApiImplicitParam(name = "employee",value = "修改的员工信息",required = true)
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){
        //获得当前用户id
//        Long empId = (Long) request.getSession().getAttribute("employee");
        log.info("员工状态{}",employee.getStatus());

        long id = Thread.currentThread().getId();
        log.info("线程ID为：{}",id);
        //1610313360639824000
//        employee.setUpdateUser(empId);
//        employee.setUpdateTime(LocalDateTime.now());

        employeeService.updateById(employee);
        return R.success("修改成功");
    }
    /**
     * 根据id查询员工信息
     * */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息接口")
    @ApiImplicitParam(name = "id",value = "员工ID",required = true)
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee= employeeService.getById(id);
        if (employee != null){
            return R.success(employee);
        }
            return R.error("没有查询到对应员工");
    }
}
