package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表dish，dish_flavor
    public void saveWithFlavor(DishDto dishDto);
    ////根据id查询菜品信息数据以及菜品口味信息数据，然后回显
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品信息，同时更新对应的口味信息
    public void updataWithFalvor(DishDto dishDto);

    public void deleteWithFlavor(String id);

    public Dish updateStatus0(String statusId);

    public Dish updateStatus1(String statusId);

    public void removeWithFlavor(List<Long> id);
}
