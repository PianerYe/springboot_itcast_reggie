package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理
 * */
@Slf4j
@RestController
@RequestMapping("category")
@Api(tags = "分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * */
    @PostMapping
    @ApiOperation(value = "新增分类接口")
    @ApiImplicitParam(name = "category",value = "菜品(套餐)分类",required = true)
    public R<String> save(@RequestBody Category category){
        log.info("category: {}",category.toString());

        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * */
    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true)
    })
    public R<Page> page(int page,int pageSize){
        log.info("page = {},pageSize = {}",page,pageSize);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //添加排序条件,根据sort升序排序
        queryWrapper.orderByAsc(Category::getSort);
        //执行查询
        categoryService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }
    /**
     * 根据ID删除分类
     */
    @DeleteMapping()
    @ApiOperation(value = "根据ID删除分类接口")
    @ApiImplicitParam(name = "id",value = "分类ID",required = true)
    public R<String> delete(Long id){
        log.info("删除分类，id为: {}",id);

//        categoryService.removeById(id);
        categoryService.remove(id);

        return R.success("分类信息删除成功");
    }
    /**
     * 根据ID修改分类信息
     * */
    @PutMapping
    @ApiOperation(value = "修改分类接口")
    @ApiImplicitParam(name = "category",value = "菜品(套餐)分类",required = true)
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息: {}",category);

        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

    /**
     * 根据条件查询分类数据
     * */
    @GetMapping("/list")
    @ApiOperation(value = "根据条件查询分类接口")
    public R<List<Category>> list(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType()!= null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

//    public R<List<Category>> list(Integer type){
//        //条件构造器
//        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
//        //添加条件
//        queryWrapper.eq(type != null,Category::getType,category.getType());
//        //添加排序条件
//        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
//
//        List<Category> list = categoryService.list(queryWrapper);
//        return R.success(list);
//    }
}
