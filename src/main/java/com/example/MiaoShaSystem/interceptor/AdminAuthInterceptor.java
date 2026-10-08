package com.example.MiaoShaSystem.interceptor;

import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        String role = CurrentUserUtil.getRole();
        if (role == null) {
            write(resp, Result.fail(ResultCode.UNAUTHORIZED));
            return false;
        }
        if (!"ADMIN".equals(role)) {
            write(resp, Result.fail(ResultCode.NO_PERMISSION));
            return false;
        }
        return true;
    }

    private void write(HttpServletResponse resp, Result result) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(objectMapper.writeValueAsString(result));
    }
}