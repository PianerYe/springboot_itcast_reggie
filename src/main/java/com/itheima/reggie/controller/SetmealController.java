package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/page")
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
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto: {}",setmealDto.toString());

        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("添加套餐成功");
    }
    /**
     * 修改套餐
     * */
    @GetMapping("/{id}")
    public R<SetmealDto> updata(@PathVariable Long id){
        log.info("根据id查询套餐信息...{}",id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    @PutMapping
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


}
