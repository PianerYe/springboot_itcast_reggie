package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.utils.QiniuUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    @Override
    @Transactional
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        //保存菜品的基本信息到套餐表setmeal
        this.save(setmealDto);
        //套餐分类setmeal_dish
        Long setmealId= setmealDto.getId();//菜品ID
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        //保存到套餐分类表setmeal_dishs
        setmealDishService.saveBatch(setmealDishes);


    }
    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //先判断菜品对应的图片信息是否做出修改
        Setmeal setmeal = this.getById(setmealDto.getId());
        log.info("图片测试！！！！！{}:::::{}",setmeal.getImage(),setmealDto.getImage());
        if (setmeal.getImage() != null && (! setmeal.getImage().equals(setmealDto.getImage()))){
            //移除菜品对应的网络图片信息
            //String fileName = basePath + File.separator + dish.getImage();
            QiniuUtils.deleteFileFromQiniu(setmeal.getImage());
        }

        //修改套餐数据
        this.updateById(setmealDto);
        //获取菜品数据信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //移除对应的套餐分类图片信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        //根据ID查询菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(setmealDishes);

        return setmealDto;
    }

    @Transactional
    @Override
    public void deleteWithDish(String id) {
        //移除套餐对应的网络图片信息
        Setmeal setmeal = this.getById(id);
        QiniuUtils.deleteFileFromQiniu(setmeal.getImage());
        //先删除套餐下对应的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        setmealDishService.remove(queryWrapper);
        //再删除套餐信息
        this.removeById(id);
    }

    @Override
    public Setmeal updateStatus0(String statusId) {
        Setmeal setmeal = this.getById(statusId);
        setmeal.setStatus(0);
        this.updateById(setmeal);
        return setmeal;
    }

    @Override
    public Setmeal updateStatus1(String statusId) {
        Setmeal setmeal = this.getById(statusId);
        setmeal.setStatus(1);
        this.updateById(setmeal);
        return setmeal;
    }
}
