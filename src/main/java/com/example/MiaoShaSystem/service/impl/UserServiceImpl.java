package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.mapper.UserMapper;
import com.example.MiaoShaSystem.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public Result register(User user){
//        1.检查用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>();
        queryWrapper.eq(User::getUsername,user.getUsername());
        if(this.getOne(queryWrapper)!=null){
//            返回1001，表示用户名已存在
            return Result.fail(ResultCode.USERNAME_EXIT);

        }
//        2.检查密码是否为空
        if(user.getPassword()==null || user.getPassword().isEmpty()){
            return Result.fail("密码不能为空");

        }
//        3.MD5加密
        String Md5= DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(Md5);
//        4.保存到数据库
        this.save(user);
        return Result.success("注册成功",null);




    }
    @Override
    public Result login(User user, HttpSession httpSession){
//        1.检查用户名是否为空
        if(user.getUsername()==null || user.getUsername().isEmpty()){
            return Result.fail("用户名不能为空");
        }
//        2.检查密码是否为空
        if(user.getPassword()==null || user.getPassword().isEmpty()){
            return Result.fail("密码不能为空");

        }
//        3.检查用户名是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        User dbuser=this.getOne(wrapper);
//        如果用户不存在
        if(dbuser==null){
            return Result.fail(ResultCode.LOGIN_ERROR);

        }
//        4.校验密码
        String Md5= DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        if (!Md5.equals(dbuser.getPassword())) {
            return Result.fail(ResultCode.USERNAME_EXIT);
        }
//        5.登录成功,存入session
        httpSession.setAttribute("userId",dbuser.getId());
        httpSession.setAttribute("userName",dbuser.getUsername());

        return Result.success("登录成功",dbuser.getId());




    }
}
