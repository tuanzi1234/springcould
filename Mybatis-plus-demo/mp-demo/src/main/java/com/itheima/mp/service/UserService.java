package com.itheima.mp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.domain.vo.UserVO;

import java.util.List;

public interface UserService extends IService<User> {
    void deductionMoney(Long id, Integer money);

    List<User> queryList(String name, Integer status, Integer minBalance, Integer maxBalance);

    UserVO queryUserAndAddressesById(Long id);

    List<UserVO> listUsersAndAddressesByIds(List<Long> ids);

    PageDTO<UserVO> queryPage(UserQuery query);
}
