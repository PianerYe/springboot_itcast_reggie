package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithSetmealDish(SetmealDto setmealDto);

    public void updateWithDish(SetmealDto setmealDto);

    public SetmealDto getByIdWithDish(Long id);

    public void deleteWithDish(String id);

    public Setmeal updateStatus0(String statusId);

    public Setmeal updateStatus1(String statusId);

    public void removeWithDish(List<Long> ids);
}
