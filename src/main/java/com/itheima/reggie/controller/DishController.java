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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 * */
@RestController
@RequestMapping("dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 菜品信息分页查询
     * */
    @GetMapping("/page")
    @ApiOperation(value = "菜品分类查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "菜品名称",required = false)
    })
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
    @ApiOperation(value = "新建菜品")
    @ApiImplicitParam(name = "dishDto",value = "菜品Dto",required = true)
    public R<String> save(@RequestBody DishDto dishDto){
        log.info("dishDto: {}",dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";//只要确认修改或者添加，前端都会默认传status=1的值传入后端
        redisTemplate.delete(key);

        return R.success("添加菜品成功");
    }
    @PutMapping
    @ApiOperation(value = "编辑菜品")
    @ApiImplicitParam(name = "dishDto",value = "菜品Dto",required = true)
    public R<String> updata(@RequestBody DishDto dishDto){
        dishService.updataWithFalvor(dishDto);

        //清理所有菜品的缓存数据
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";//只要确认修改或者添加，前端都会默认传status=1的值传入后端
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    /**
     * 根据ID菜品数据回显到表单
     * */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品ID查询菜品接口")
    @ApiImplicitParam(name = "id",value = "菜品ID",required = true)
    public R<DishDto> updata(@PathVariable Long id){
        log.info("根据id查询菜品信息...");
        //根据id查询菜品信息数据以及菜品口味信息数据，然后回显
        DishDto dto = dishService.getByIdWithFlavor(id);
        return R.success(dto);
    }

    /**修改菜品禁售状态*/
    @PostMapping("/status/0")
    @ApiOperation("设置菜品状态为禁售")
    @ApiImplicitParam(name = "ids",value = "菜品ID的集合",required = false)
    public R<List<Dish>> updateStatus0(String ids){
        log.info("传递的ids:{}",ids);
        String[] statusIds = ids.split(",");
        List<Dish> dishList = new ArrayList<>();
        for (String statusId: statusIds) {
            Dish dish1 = dishService.getById(statusId);
            if (dish1.getStatus() != 0){
                //不需要清理数据了
                //清理所有菜品的缓存数据
                //Set keys = redisTemplate.keys("dish_*");
                //redisTemplate.delete(keys);

                //清理某个分类下面的菜品缓存数据
                String key = "dish_" + dish1.getCategoryId() + "_1";//只要确认修改或者添加，前端都会默认传status=1的值传入后端
                redisTemplate.delete(key);
            }



            Dish dish = dishService.updateStatus0(statusId);
            dishList.add(dish);
        }
        return R.success(dishList);
    }

    /**修改菜品启售状态*/
    @PostMapping("/status/1")
    @ApiOperation("设置菜品状态为启售")
    @ApiImplicitParam(name = "ids",value = "菜品ID的集合",required = false)
    public R<List<Dish>> updateStatus1(String ids){
        log.info("传递的ids:{}",ids);
        String[] statusIds = ids.split(",");
        List<Dish> dishList = new ArrayList<>();
        for (String statusId: statusIds) {
            Dish dish1 = dishService.getById(statusId);
            if (dish1.getStatus() != 1){
                //不需要清理数据了
                //清理所有菜品的缓存数据
                //Set keys = redisTemplate.keys("dish_*");
                //redisTemplate.delete(keys);

                //清理某个分类下面的菜品缓存数据
                String key = "dish_" + dish1.getCategoryId() + "_1";//只要确认修改或者添加，前端都会默认传status=1的值传入后端
                redisTemplate.delete(key);
            }


            Dish dish = dishService.updateStatus1(statusId);

            dishList.add(dish);
        }
        return R.success(dishList);
    }

    /**
     * 删除和批量删除菜品
     * *//*
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
    }*/

//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        log.info("categoryId:{}",dish.getCategoryId());
//        //条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        //添加条件
//        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        //查询状态是1的的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//        return R.success(list);
//    }

    /**
     * 删除和批量删除菜品
     * */
    @DeleteMapping
    @ApiOperation("删除和批量删除菜品")
    @ApiImplicitParam(name = "ids",value = "菜品集合",required = false)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        if (ids == null){
            return R.error("没有选择删除的菜品");
        }

        //推测代码冗余，优化性能建议注释掉
       /* for (Long id : ids) {

            Dish dish = dishService.getById(id);
            //清理所有菜品的缓存数据
            //Set keys = redisTemplate.keys("dish_*");
            //redisTemplate.delete(keys);

            //清理某个分类下面的菜品缓存数据
            String key = "dish_" + dish.getCategoryId() + "_1";//只要确认修改或者添加，前端都会默认传status=1的值传入后端
            redisTemplate.delete(key);
        }*/

        //执行删除，先删除菜品信息，然后清理当前菜品对应的口味数据
        dishService.removeWithFlavor(ids);




        return R.success("菜品删除成功");
    }

    @GetMapping("/list")
    @ApiOperation("查询菜品数据接口")
    public R<List<DishDto>> list(Dish dish){
        log.info("categoryId:{}",dish.getCategoryId());

        List<DishDto> dishDtoList  = null;

        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_1397844263642378242_1

        //先从redis获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        //如果存在，直接返回，无需查询数据库
        if (dishDtoList != null){
            return R.success(dishDtoList);
        }
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //查询状态是1的的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item)->{
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            //根据id查询分类对象
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            Long dishId = item.getId();
            //根据id查询菜品口味数据
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，查询数据库，将查询到的菜品数据缓存到redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }
}
