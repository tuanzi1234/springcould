package com.itheima.mp.service.Impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.po.Address;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.domain.vo.AddressVO;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.enums.UserStatus;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 扣金额
    @Override
    public void deductionMoney(Long id, Integer money) {
        // 1.查询用户
        User user = this.getById(id);
        // 2.检查用户状态
        if (user == null || user.getStatus() == UserStatus.FROZEN) {
            throw new RuntimeException("用户状态异常");
        }
        // 3.检查余额是否充足
        if (user.getBalance() < money) {
            throw new RuntimeException("余额不足");
        }
        // 4.更新用户余额, 若用户余额变成0，则将用户状态改为2
        int remainBalance = user.getBalance() - money;
        lambdaUpdate().set(User::getBalance, remainBalance)
                .set(remainBalance == 0, User::getStatus, 2)
                .eq(User::getId, id)
                .eq(User::getBalance, user.getBalance())
                .update();
    }

    // 查询用户列表
    @Override
    public List<User> queryList(String name, Integer status, Integer minBalance, Integer maxBalance) {
        return lambdaQuery().like(name != null, User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .gt(minBalance != null, User::getBalance, minBalance)
                .le(maxBalance != null, User::getBalance, maxBalance)
                .list();
    }

    // 查询用户详情
    @Override
    public UserVO queryUserAndAddressesById(Long id) {
        // 1.查询用户
        User user = this.getById(id);
        if (user == null || user.getStatus() == UserStatus.FROZEN){
            throw new RuntimeException("用户状态异常");
        }
        // 2.查询用户地址
        List<Address> addresses = Db.lambdaQuery(Address.class)
                .eq(Address::getUserId, id).list();
        // 3.检查用户状态，若为2，则抛出用户状态异常
        if (user.getStatus() == UserStatus.FROZEN){
            throw new RuntimeException("用户状态异常");
        }
        // 4.封装数据
        // 封装用户
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        // 封装地址
        if (CollUtil.isNotEmpty(addresses)){
            userVO.setAddresses(BeanUtil.copyToList(addresses, AddressVO.class));
        }
        return userVO;
    }

    // 批量查询用户详情和地址
    @Override
    public List<UserVO> listUsersAndAddressesByIds(List<Long> ids) {
        // 1.查询用户
        List<User> users = listByIds(ids);
        if (CollUtil.isEmpty(users)){
            return null;
        }
        // 2.查询用户地址
        // 获取用户id
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        // 查询地址
        List<Address> addresses = Db.lambdaQuery(Address.class).in(Address::getUserId, userIds).list();
        // 3.封装用户
        return users.stream().map(user -> {
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            userVO.setAddresses(addresses.stream().filter(address -> address.getUserId().equals(user.getId()))
                    .map(address -> BeanUtil.copyProperties(address, AddressVO.class))
                    .collect(Collectors.toList()));
            return userVO;
        }).collect(Collectors.toList());
    }

    // 分页查询用户
    @Override
    public PageDTO<UserVO> queryPage(UserQuery query) {
        String name = query.getName();
        Integer status = query.getStatus();
        // 1.构建查询条件
        // 分页条件
        Page<User> page = Page.of(query.getPageNo(), query.getPageSize());
        // 排序条件
        if (StrUtil.isNotBlank(query.getSortBy())){
            // 不为空
            page.addOrder(new OrderItem(query.getSortBy(), query.getIsAsc()));
        } else {
            // 为空
            page.addOrder(new OrderItem("update_time", false));
        }
        // 2.分页查询
        Page<User> p = lambdaQuery().like(name != null, User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .page(page);
        // 3.封装数据
        PageDTO<UserVO> pageDTO = new PageDTO<>();
        pageDTO.setTotal(p.getTotal()); // 总数
        pageDTO.setPages(p.getPages()); // 总页数
        List<User> records = p.getRecords(); // 当前页数据
        if (CollUtil.isEmpty(records)){
            pageDTO.setList(Collections.emptyList());
            return pageDTO;
        }
        pageDTO.setList(BeanUtil.copyToList(records, UserVO.class));
        return pageDTO;
    }
}
