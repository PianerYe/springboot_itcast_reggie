package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("setmeal")
@Slf4j
@Api(tags = "套餐相关接口")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private DishService dishService;
    @GetMapping("/page")
    @ApiOperation(value = "套餐分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "套餐名称",required = false)
    })
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        //添加排序条件,根据操作时间降序排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            //根据id查询套餐分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }
    /**
     * 添加套餐
     * */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation(value = "新增套餐接口")
    @ApiImplicitParam(name = "setmealDto",value = "套餐名称",required = true)
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto: {}",setmealDto.toString());

        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("添加套餐成功");
    }
    /**
     * 修改套餐
     * */
    @GetMapping("/{id}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation("查询套餐接口")
    @ApiImplicitParam(name = "id",value = "套餐ID",required = true)
    public R<SetmealDto> updata(@PathVariable Long id){
        log.info("根据id查询套餐信息...{}",id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    @PutMapping
    @ApiOperation("编辑套餐接口")
    @ApiImplicitParam(name = "setmealDto",value = "套餐Dto",required = true)
    public R<String> updata(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto:{}",setmealDto);
        setmealService.updateWithDish(setmealDto);
        return R.success("套餐修改成功");
    }

//    @DeleteMapping()
//    public R<String> delete(String ids){
//        log.info("ids:{}",ids);
//
//        if (ids == null){
//            return R.error("没有选择要删除的套餐");
//        }
//        String[] deleteids = ids.split(",");
//        for (String id: deleteids) {
//            Setmeal setmeal = setmealService.getById(id);
//            if (setmeal.getStatus() == 1){
//                return R.error("有启售的套餐，不允许删除");
//            }
//            //执行删除，先删除套餐信息，然后清理当前套餐对应的菜品数据
//            setmealService.deleteWithDish(id);
//        }
//        return R.success("套餐删除成功");
//    }

    @DeleteMapping()
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation("套餐删除接口")
    @ApiImplicitParam(name = "ids",value = "套餐ID的集合",required = false)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        if (ids == null){
            return R.error("没有选择要删除的套餐");
        }
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }


    //**修改套餐禁售状态*//*
    @PostMapping("/status/0")
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation("套餐禁售接口")
    @ApiImplicitParam(name = "ids",value = "套餐id的集合",required = false)
    public R<List<Setmeal>> updateStatus0(String ids){
        log.info("传递的ids:{}",ids);
        String[] statusIds = ids.split(",");
        List<Setmeal> setmealList = new ArrayList<>();
        for (String statusId: statusIds) {
            Setmeal setmeal = setmealService.updateStatus0(statusId);
            setmealList.add(setmeal);
        }
        return R.success(setmealList);
    }

    //**修改菜品启售状态*//*
    @PostMapping("/status/1")
    @CacheEvict(value = "setmealCache",allEntries = true)
    @ApiOperation("套餐启售接口")
    @ApiImplicitParam(name = "ids",value = "套餐id的集合",required = false)
    public R<List<Setmeal>> updateStatus1(String ids){
        log.info("传递的ids:{}",ids);
        String[] statusIds = ids.split(",");
        List<Setmeal> setmealList = new ArrayList<>();
        for (String statusId: statusIds) {
            Setmeal setmeal = setmealService.updateStatus1(statusId);
            setmealList.add(setmeal);
        }
        return R.success(setmealList);
    }


    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+ '_' + #setmeal.status")
    @ApiOperation("套餐条件查询接口")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("categoryId:{}",setmeal.getCategoryId());
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        //查询状态是1的的菜品
        queryWrapper.eq(Setmeal::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

/*    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        log.info("categoryId:{}",setmeal.getCategoryId());
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        //查询状态是1的的菜品
        queryWrapper.eq(Setmeal::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);

        List<SetmealDto> setmealDtoList = list.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);

            Long setmealId = item.getId();
            //根据id查询分类对象
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealId);
            List<SetmealDish> setmealDishes = setmealDishService.list(lambdaQueryWrapper);
            setmealDto.setSetmealDishes(setmealDishes);

            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(setmealDtoList);
    }*/

    @GetMapping("/dish/{id}")
    @ApiOperation("查询菜品列表")
    @ApiImplicitParam(name = "id",value = "套餐id",required = true)
    public R<List<SetmealDishDto>> dish(@PathVariable Long id){
        log.info("setmealId:{}",id);

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        List<SetmealDishDto> setmealDishDtos = list.stream().map((item) -> {
            SetmealDishDto setmealDishDto = new SetmealDishDto();
            BeanUtils.copyProperties(item, setmealDishDto);
            Long dishId = item.getDishId();
            Dish dish = dishService.getById(dishId);
            setmealDishDto.setImage(dish.getImage());
            return setmealDishDto;
        }).collect(Collectors.toList());

        return R.success(setmealDishDtos);
    }

}
