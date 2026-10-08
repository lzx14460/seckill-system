package com.example.MiaoShaSystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.example.MiaoShaSystem.dto.LoginDTO;
import com.example.MiaoShaSystem.dto.RegisterDTO;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.mapper.UserMapper;
import com.example.MiaoShaSystem.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

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
    public Result register(RegisterDTO dto) {
        // 1. 基础校验
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) {
            return Result.fail("手机号不能为空");
        }
        if (!dto.getPhone().matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }

        // 2. 角色校验
        String role = dto.getRole();
        if (!"USER".equals(role) && !"MERCHANT".equals(role)) {
            return Result.fail("身份不合法");
        }
        if ("MERCHANT".equals(role)) {
            if (dto.getShopName() == null || dto.getShopName().trim().isEmpty()) {
                return Result.fail("商家必须填写商铺名称");
            }
        }

        // 3. 用户名唯一
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, dto.getUsername());
        if (this.getOne(qw) != null) {
            return Result.fail(ResultCode.USERNAME_EXIT);
        }

        // 4. 手机号唯一（可选）
        LambdaQueryWrapper<User> phoneQw = new LambdaQueryWrapper<>();
        phoneQw.eq(User::getPhone, dto.getPhone());
        if (this.getOne(phoneQw) != null) {
            return Result.fail("手机号已被注册");
        }

        // 5. 入库
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes()));
        user.setRole(role);
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        if ("MERCHANT".equals(role)) {
            user.setShopName(dto.getShopName());
        }
        // 商家需要审核，买家免审
        if ("MERCHANT".equals(role)) {
            user.setAuditStatus((byte) 0);   // 待审核
        } else {
            user.setAuditStatus((byte) 1);   // 已通过
        }
        user.setBanned((byte) 0);
        this.save(user);
        System.out.println("注册参数: " + dto);
        return Result.success("注册成功", null);
    }

    @Override
    public Result login(LoginDTO dto, HttpSession session) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        if (dto.getRole() == null || dto.getRole().isEmpty()) {
            return Result.fail("请选择登录身份");
        }

        // 1. 查用户
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, dto.getUsername());
        User dbuser = this.getOne(qw);
        if (dbuser == null) {
            return Result.fail(ResultCode.LOGIN_ERROR);
        }

        // 2. 校验密码
        String md5 = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes());
        if (!md5.equals(dbuser.getPassword())) {
            return Result.fail(ResultCode.LOGIN_ERROR);
        }

        // 3. ★ 校验身份 ★
        if (!dto.getRole().equals(dbuser.getRole())) {
            String realRole = "MERCHANT".equals(dbuser.getRole()) ? "商家" : "买家";
            return Result.fail("该账号是「" + realRole + "」，请选择正确身份登录");
        }
        // 封禁校验
        if (dbuser.getBanned() != null && dbuser.getBanned() == 1) {
            return Result.fail("账号已被封禁，请联系管理员");
        }

        // 商家审核校验
        if ("MERCHANT".equals(dbuser.getRole())) {
            if (dbuser.getAuditStatus() == null || dbuser.getAuditStatus() == 0) {
                return Result.fail("商家账号审核中，请等待管理员通过");
            }
            if (dbuser.getAuditStatus() == 2) {
                String remark = dbuser.getAuditRemark();
                return Result.fail("商家审核未通过" + (remark != null && !remark.isEmpty() ? "：" + remark : ""));
            }
        }

        // 4. 写 Session
        session.setAttribute("userId", dbuser.getId());
        session.setAttribute("userName", dbuser.getUsername());
        session.setAttribute("role", dbuser.getRole());
        session.setAttribute("shopName", dbuser.getShopName());

        // 5. 返回
        Map<String, Object> data = new HashMap<>();
        data.put("userId", dbuser.getId());
        data.put("userName", dbuser.getUsername());
        data.put("role", dbuser.getRole());
        data.put("shopName", dbuser.getShopName());
        return Result.success("登录成功", data);
    }
}
