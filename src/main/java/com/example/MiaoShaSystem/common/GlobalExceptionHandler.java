package com.example.MiaoShaSystem.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常：返回业务码 */
    @ExceptionHandler(BizException.class)
    public Result handleBiz(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        Result r = new Result();
        r.setCode(e.getCode());
        r.setMessage(e.getMessage());
        return r;
    }

    /** 参数异常 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /** 兜底：其他未捕获的异常 */
    @ExceptionHandler(Exception.class)
    public Result handleAll(Exception e) {
        log.error("系统异常", e);
        Result r = new Result();
        r.setCode(ResultCode.FAIL.getCode());
        r.setMessage("系统繁忙，请稍后重试");
        return r;
    }
}