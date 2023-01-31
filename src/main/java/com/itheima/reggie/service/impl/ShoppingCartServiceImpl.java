package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private OrderDetailService orderDetailService;


    @Override
    @Transactional
    public void saveWithAgain(Long id) {
        //将原先订单中下单的菜品数据（order_detail），口味，数量重新放入购物车
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        List<ShoppingCart> shoppingCartList = list.stream().map((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            shoppingCart.setName(item.getName());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setSetmealId(item.getSetmealId());
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setNumber(item.getNumber());

            shoppingCart.setCreateTime(LocalDateTime.now());
            log.info("shoppingCart的数量查看:{}",shoppingCart);
            return shoppingCart;
        }).collect(Collectors.toList());
        log.info("shoppingCart的数量查看:{}",shoppingCartList);
        this.saveBatch(shoppingCartList);
    }
}
