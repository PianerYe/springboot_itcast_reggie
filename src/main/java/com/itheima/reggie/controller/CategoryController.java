package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
/**
 * 分类管理
 * */
@Slf4j
@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category: {}",category.toString());

        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 分页查询
     * */
    @GetMapping("/page")
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
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息: {}",category);

        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }
}