package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表dish，dish_flavor
    public void saveWithFlavor(DishDto dishDto);
    ////根据id查询菜品信息数据以及菜品口味信息数据，然后回显
    public void getByIdWithFlavor(Long id);

}
