package com.example.MiaoShaSystem.aspect;

import com.example.MiaoShaSystem.annotation.OpLog;
import com.example.MiaoShaSystem.common.CurrentUserUtil;
import com.example.MiaoShaSystem.entity.OperationLog;
import com.example.MiaoShaSystem.mapper.OperationLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogMapper logMapper;

    @Around("@annotation(opLog)")
    public Object around(ProceedingJoinPoint pjp, OpLog opLog) throws Throwable {
        Object result = pjp.proceed();

        try {
            OperationLog log = new OperationLog();
            log.setOperatorId(CurrentUserUtil.getUserId());
            log.setOperatorName(CurrentUserUtil.getUserName());
            log.setOperatorRole(CurrentUserUtil.getRole());
            log.setAction(opLog.action());
            log.setTargetType(opLog.targetType());
            log.setDetail(opLog.detail());
            log.setCreatedAt(LocalDateTime.now());

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                log.setIp(getClientIp(req));
            }

            logMapper.insert(log);
        } catch (Exception e) {
            // 日志失败不影响业务
        }

        return result;
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = req.getRemoteAddr();
        return ip;
    }
}