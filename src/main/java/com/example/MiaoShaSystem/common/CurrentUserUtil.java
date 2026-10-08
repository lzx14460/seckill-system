package com.example.MiaoShaSystem.common;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class CurrentUserUtil {

    public static Long getUserId() {
        HttpSession session = getSession();
        if (session == null) return null;
        Object uid = session.getAttribute("userId");
        return uid == null ? null : Long.valueOf(uid.toString());
    }

    public static String getRole() {
        HttpSession session = getSession();
        if (session == null) return null;
        Object role = session.getAttribute("role");
        return role == null ? null : role.toString();
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest().getSession(false);
    }
    public static String getUserName() {
        HttpSession session = getSession();
        if (session == null) return null;
        Object name = session.getAttribute("userName");
        return name == null ? null : name.toString();
    }
}