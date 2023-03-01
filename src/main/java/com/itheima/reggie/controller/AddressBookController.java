package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("addressBook")
@Api(tags = "地址簿相关接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     * */
    @PostMapping
    @ApiOperation("新增地址簿接口")
    @ApiImplicitParam(name = "addressBook",value = "地址簿",required = true)
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());

        log.info("addressBook:{}",addressBook);

        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     * */
    @PutMapping("/default")
    @Transactional
    @ApiOperation("设置默认地址接口")
    @ApiImplicitParam(name = "addressBook",value = "地址簿",required = true)
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("addressBook:{}",addressBook);

        LambdaUpdateWrapper<AddressBook> wrapper= new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);

        return R.success(addressBook);
    }

    /**
     * 根据ID查询地址
     * */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询地址接口")
    @ApiImplicitParam(name = "id",value = "地址簿ID",required = true)
    public R<AddressBook> get(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null){
            return R.success(addressBook);
        }else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     * */
    @GetMapping("/default")
    @ApiOperation("查询默认地址接口")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault,1);

        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook == null){
            return R.error("没有找到该对象");
        }else {
            return R.success(addressBook);
        }
    }


    @GetMapping("/list")
    @ApiOperation("查询地址簿列表接口")
    public R<List<AddressBook>> list(AddressBook addressBook, HttpSession session){
        addressBook.setUserId((Long) session.getAttribute("user"));
        log.info("addressBook:{}",addressBook);

        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook.getUserId() != null,AddressBook::getUserId,addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 修改地址信息
     * */
    @PutMapping
    @ApiOperation("修改地址接口")
    @ApiImplicitParam(name = "addressBook",value = "地址簿",required = true)
    public R<String> update(@RequestBody AddressBook addressBook){
        log.info("addressBook:{}",addressBook);

        addressBookService.updateById(addressBook);
        return R.success("地址修改成功");
    }

    /**
     * 删除地址
     * */
    @DeleteMapping
    @ApiOperation("删除地址接口")
    @ApiImplicitParam(name = "ids",value = "地址簿ID集合",required = true)
    public R<String> delete(Long ids){
        log.info("ids:{}",ids);
        addressBookService.removeById(ids);
        return R.success("地址删除成功");
    }
}
