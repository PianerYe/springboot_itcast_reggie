package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查看购物车
     * */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 添加购物车
     * */
    @PostMapping("/add")
    public R<ShoppingCart> save(@RequestBody ShoppingCart shoppingCart){
        //设置用户ID，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        if (dishId != null){
            //添加到购物车的是菜品
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            //添加到购物车的是套餐
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        ShoppingCart cartServiceOne = shoppingCartService.getOne(lambdaQueryWrapper);

        if (cartServiceOne != null){
            //如果已经存在，就在原来数量基础上+1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果不存在，则添加到购物车，数量默认就是1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 购物车商品数量减少
     * */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //查询当前菜品或者套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if (dishId != null){
            //减1到购物车的菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            ShoppingCart shoppingCartDish = shoppingCartService.getOne(queryWrapper);
            Integer number = shoppingCartDish.getNumber();
            if (number >1){
                number = number - 1;
                shoppingCartDish.setNumber(number);
                shoppingCartDish.setCreateTime(LocalDateTime.now());
                shoppingCartService.updateById(shoppingCartDish);
            }else if (number == 1){
                //
                shoppingCartService.removeById(shoppingCartDish.getId());
                shoppingCartDish.setNumber(0);
            }
            return R.success(shoppingCartDish);
        }else {
            //添加到购物车的是套餐
            Long setmealId = shoppingCart.getSetmealId();
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            ShoppingCart shoppingCartSetmeal = shoppingCartService.getOne(queryWrapper);
            Integer number = shoppingCartSetmeal.getNumber();
            if (number >1){
                number = number - 1;
                shoppingCartSetmeal.setNumber(number);
                shoppingCartSetmeal.setCreateTime(LocalDateTime.now());
                shoppingCartService.updateById(shoppingCartSetmeal);
            }else if (number == 1){
                //
                shoppingCartService.removeById(shoppingCartSetmeal.getId());
                shoppingCartSetmeal.setNumber(0);
            }
            return R.success(shoppingCartSetmeal);
        }

    }
}
