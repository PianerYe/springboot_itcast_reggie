package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("order")
@Api(tags = "订单相关接口")
public class OrderController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    /**
     * 用户下单
     * */
    @PostMapping("submit")
    @ApiOperation("用户下单")
    @ApiImplicitParam(name = "orders",value = "订单",required = true)
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 手机端查看订单
     * */
    @GetMapping("/userPage")
    @ApiOperation("手机端查看订单接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页包含订单条数",required = true)
    })
    public R<Page> page(int page,int pageSize){
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> pageInfoDto = new Page<>();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getCheckoutTime);
        ordersService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo,pageInfoDto,"records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            Long orderId = item.getId();
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(orderId != null, OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper);
            ordersDto.setOrderDetails(orderDetails);
            //SELECT SUM(number) FROM order_dataile WHERE id = orderId;
            QueryWrapper<OrderDetail> wq = new QueryWrapper<>();
            wq.select("sum(number) AS subNum").eq("order_id", orderId);
            Map<String, Object> map = orderDetailService.getMap(wq);
            BigDecimal subNum = (BigDecimal) map.get("subNum");
            System.out.println("测试！！！！！！！！！！！：" + subNum);
            ordersDto.setSubNum(subNum.intValue());
            return ordersDto;
        }).collect(Collectors.toList());

        pageInfoDto.setRecords(list);

        return R.success(pageInfoDto);
    }

    @PostMapping("/again")
    @ApiOperation("再次添加订单接口")
    @ApiImplicitParam(name = "orders",value = "订单",required = true)
    public R<String> again(@RequestBody Orders orders){
        log.info("id是：{}",orders.getId());
        Long id = orders.getId();
        shoppingCartService.saveWithAgain(id);
        return R.success("添加成功！");
    }

    @GetMapping("/page")
    @ApiOperation("查询订单接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页显示条数",required = true),
            @ApiImplicitParam(name = "number",value = "订单号",required = false),
            @ApiImplicitParam(name = "beginTime",value = "开始时间",required = false),
            @ApiImplicitParam(name = "beginTime",value = "结束时间",required = false)
    })
    public R<Page> page(int page,int pageSize,String number,String beginTime,String endTime){
        log.info("page = {},pageSize = {},number = {},beginTime = {},endTime = {}",page,pageSize,number,beginTime,endTime);
        //构造分页构造器
        Page<Orders>  pageInfo = new Page<>(page,pageSize);
//        Page<OrdersDto> pageInfoDto = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
            queryWrapper.like(number != null,Orders::getNumber,number);
            queryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime,beginTime,endTime);
            queryWrapper.orderByDesc(Orders::getOrderTime);
            ordersService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping
    @ApiOperation("订单状态变更")
    @ApiImplicitParam(name = "orders",value = "订单",required = true)
    public R<String> updatStatus(@RequestBody Orders orders){
        log.info("orders.getId():{},orders.getStatus():{}",orders.getId(),orders.getStatus());
        LambdaUpdateWrapper<Orders> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Orders::getId,orders.getId());
        wrapper.set(Orders::getStatus,orders.getStatus());
        ordersService.update(wrapper);

        return R.success("订单派送中");

    }
}

