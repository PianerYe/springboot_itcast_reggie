package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.ISqlSegment;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrdersService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("order")
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
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}",orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 手机端查看订单
     * */
    @GetMapping("/userPage")
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
    public R<String> again(@RequestBody Orders orders){
        log.info("id是：{}",orders.getId());
        Long id = orders.getId();
        shoppingCartService.saveWithAgain(id);
        return R.success("添加成功！");
    }
}
