package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.LoginDTO;
import com.example.MiaoShaSystem.dto.RegisterDTO;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.mapper.UserMapper;
import com.example.MiaoShaSystem.service.IUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private IUserService userService;
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO dto, HttpSession session) {
        return userService.login(dto, session);
    }
    @GetMapping("/logout")
    public Result logout(HttpSession httpSession){
        httpSession.invalidate();
        return Result.success("已退出",null);
    }

}
