package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * */
@RestController
@RequestMapping("dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 菜品信息分页查询
     * */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pageSize = {},name = {}",page,pageSize,name);
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        //添加排序条件,根据sort升序排序
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 新建菜品
     * */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("dishDto: {}",dishDto.toString());

        dishService.saveWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }
    @PutMapping
    public R<String> updata(@RequestBody DishDto dishDto){
        dishService.updataWithFalvor(dishDto);
        return R.success("修改菜品成功");
    }

    /**
     * 根据ID菜品数据回显到表单
     * */
    @GetMapping("/{id}")
    public R<DishDto> updata(@PathVariable Long id){
        log.info("根据id查询菜品信息...");
        //根据id查询菜品信息数据以及菜品口味信息数据，然后回显
        DishDto dto = dishService.getByIdWithFlavor(id);
        return R.success(dto);
    }

    /**修改菜品禁售状态*/
    @PostMapping("/status")
    public R<String> update(HttpServletRequest request,@RequestBody DishDto dishDto){
        log.info("是否启售{}",dishDto.getStatus());
        return null;
    }
    /**
     * 删除和批量删除
     * */
    @Transactional
    @DeleteMapping
    public R<String> delete(String ids){
        log.info("ids:{}",ids);
        if (ids == null){
            return R.error("没有选择删除的菜品");
        }
        String[] deleteids = ids.split(",");
        for (String id: deleteids) {
            //判断要删除的菜品ID是否和套餐关联
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId,id);
            SetmealDish setmealDish = setmealDishService.getOne(queryWrapper);
            if (setmealDish != null){
                return R.error("菜品和套餐关联，无法删除");
            }
            //执行删除，先删除菜品信息，然后清理当前菜品对应的口味数据
            dishService.deleteWithFlavor(id);
        }
        return R.success("菜品删除成功");
    }
}
