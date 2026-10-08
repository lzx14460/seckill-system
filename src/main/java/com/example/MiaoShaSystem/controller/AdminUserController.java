package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.example.MiaoShaSystem.annotation.OpLog;
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    @Autowired
    private IUserService userService;

    /** 用户列表 */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Byte banned,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "USER")   // 只看买家
                .eq(banned != null, User::getBanned, banned)
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .orderByDesc(User::getId);

        IPage<User> page = userService.page(new Page<>(pageNum, pageSize), qw);
        return Result.success(page);
    }

    /** 封禁 / 解封 */
    @PutMapping("/{id}/ban")
    @OpLog(action = "BAN_USER", targetType = "USER")
    public Result ban(@PathVariable Long id, @RequestParam Byte banned) {
        if (banned == null || (banned != 0 && banned != 1)) {
            throw new BizException("状态不合法");
        }

        User user = userService.getById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!"USER".equals(user.getRole())) {
            throw new BizException("只能封禁普通用户");
        }

        user.setBanned(banned);
        userService.updateById(user);

        return Result.success(banned == 1 ? "已封禁" : "已解封", null);
    }
}