package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.utils.QiniuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private SetmealDishService setmealDishService;
//    @Value("${reggie.path}")
//    private String basePath;
    /**
     * 新增菜品同时保存对应的口味数据
     * */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();//菜品ID
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavors
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //根据ID先查询菜品信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //根据ID查询口味信息，封装成集合
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updataWithFalvor(DishDto dishDto) {
        //先判断菜品对应的图片信息是否做出修改
        Dish dish = this.getById(dishDto.getId());
        log.info("图片测试！！！！！{}:::::{}",dish.getImage(),dishDto.getImage());
        if (dish.getImage() != null && (! dish.getImage().equals(dishDto.getImage()))){
            //移除菜品对应的网络图片信息
            //String fileName = basePath + File.separator + dish.getImage();
            QiniuUtils.deleteFileFromQiniu(dish.getImage());
        }

        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应的口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据---dish_flavor表的插入操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    @Transactional
    @Override
    public void deleteWithFlavor(String id) {
        //1.清理当前菜品对应的口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        dishFlavorService.remove(queryWrapper);
        //2.清理当前菜品数据信息
        //2.先移除菜品对应的网络图片信息
        Dish dish = this.getById(id);
//        String fileName = basePath + File.separator + dish.getImage();
        QiniuUtils.deleteFileFromQiniu(dish.getImage());
        //然后移除菜品
        this.removeById(id);
    }

    @Override
    public Dish updateStatus0(String statusId) {
        Dish dish = this.getById(statusId);
        dish.setStatus(0);
        this.updateById(dish);
        return dish;
    }

    @Override
    public Dish updateStatus1(String statusId) {
        Dish dish = this.getById(statusId);
        dish.setStatus(1);
        this.updateById(dish);
        return dish;
    }

    @Transactional
    @Override
    public void removeWithFlavor(List<Long> ids) {

        //判断要删除的菜品ID是否和套餐关联
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(queryWrapper1);
        if (count1 > 0){
            throw new CustomException("菜品和套餐关联，无法删除");
        }

        //
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(Dish::getId,ids);
        lambdaQueryWrapper.eq(Dish::getStatus,1);
        int count = this.count(lambdaQueryWrapper);
        if (count >0){
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        //1.清理当前菜品对应的口味数据---dish_flavor表的delete操作
        //2.先移除菜品对应的网络图片信息
        for (Long id:ids){
            Dish dish = this.getById(id);
            QiniuUtils.deleteFileFromQiniu(dish.getImage());
        }
        //然后移除菜品
        this.removeByIds(ids);
        //3.清理当前菜品数据信息
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
    }

}
