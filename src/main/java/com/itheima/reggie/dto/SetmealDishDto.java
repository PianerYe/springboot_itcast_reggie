package com.itheima.reggie.dto;

import com.itheima.reggie.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("套餐菜品关系Dto")
public class SetmealDishDto extends SetmealDish {

    @ApiModelProperty("菜品的图片地址")
    private String image; //Dish的图片地址
}
