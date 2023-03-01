package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("订单Dto")
public class OrdersDto extends Orders {

    @ApiModelProperty("下单用户")
    private String userName;

    @ApiModelProperty("下单电话")
    private String phone;

    @ApiModelProperty("收货地址")
    private String address;

    @ApiModelProperty("收货人")
    private String consignee;

    @ApiModelProperty("订单明细")
    private List<OrderDetail> orderDetails;

    @ApiModelProperty("提交用户编号")
    private Integer subNum;

    @ApiModelProperty("下单菜品")
    private List<Dish> dishList;
}
