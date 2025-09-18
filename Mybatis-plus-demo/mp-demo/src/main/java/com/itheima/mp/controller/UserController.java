package com.itheima.mp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.dto.UserFormDTO;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.vo.UserVO;

import java.util.List;

@Api(tags = "用户管理")
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 新增用户
    @ApiOperation("新增用户接口")
    @PostMapping
    public void addUser(@RequestBody UserFormDTO userDTO) {
        // 1.将DTO拷贝进PO
        User user = BeanUtil.copyProperties(userDTO, User.class);
        userService.save(user);
    }

    // 删除用户
    @ApiOperation("删除用户接口")
    @PostMapping("{id}")
    public void deleteUser(@ApiParam("用户id") @PathVariable Long id) {
        userService.removeById(id);
    }

    // 根据id查询用户以及对应的地址
    @ApiOperation("根据id查询用户接口")
    @GetMapping("{id}")
    public UserVO getUserById(@ApiParam("用户id") @PathVariable Long id) {
       return userService.queryUserAndAddressesById(id);
    }

    // 批量查询用户列表和对应地址
    @ApiOperation("批量查询用户列表接口")
    @GetMapping
    public List<UserVO> listUsers(@ApiParam("用户id集合") @RequestParam List<Long> ids) {
       return userService.listUsersAndAddressesByIds(ids);
    }

    // 扣金额
    @ApiOperation("扣金额接口")
    @PostMapping("{id}/deduction/{money}")
    public void deductionMoney(@ApiParam("用户id") @PathVariable("id") Long id,
                         @ApiParam("金额") @PathVariable("money") Integer money) {
        userService.deductionMoney(id, money);
    }

    // 复杂条件查询用户
    @ApiOperation("复杂条件查询用户接口")
    @GetMapping("/list")
    public List<UserVO> listUsers(@ApiParam("用户查询条件") UserQuery query) {
        List<User> users = userService.queryList(query.getName(), query.getStatus(), query.getMinBalance(), query.getMaxBalance());
        return BeanUtil.copyToList(users, UserVO.class);
    }

    // 分页查询用户
    @ApiOperation("分页查询用户接口")
    @GetMapping("/page")
    public PageDTO<UserVO> pageUsers(@ApiParam("用户查询条件") UserQuery query) {
        return userService.queryPage(query);
    }
}
