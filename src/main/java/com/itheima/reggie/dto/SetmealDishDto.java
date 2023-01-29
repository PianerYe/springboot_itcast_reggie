package com.itheima.reggie.dto;

import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;

@Data
public class SetmealDishDto extends SetmealDish {

    private String image; //Dish的图片地址
}
