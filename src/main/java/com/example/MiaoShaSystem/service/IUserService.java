package com.example.MiaoShaSystem.service;

import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.LoginDTO;
import com.example.MiaoShaSystem.dto.RegisterDTO;
import com.example.MiaoShaSystem.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author lzx
 * @since 2026-10-03
 */
public interface IUserService extends IService<User> {
    Result register(RegisterDTO dto);
    Result login(LoginDTO dto, HttpSession session);
}
