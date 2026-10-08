package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.entity.OperationLog;
import com.example.MiaoShaSystem.service.IOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/log")
public class AdminLogController {

    @Autowired
    private IOperationLogService logService;

    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<OperationLog> qw = new LambdaQueryWrapper<OperationLog>()
                .eq(StringUtils.hasText(action), OperationLog::getAction, action)
                .like(StringUtils.hasText(keyword), OperationLog::getOperatorName, keyword)
                .orderByDesc(OperationLog::getCreatedAt);

        return Result.success(logService.page(new Page<>(pageNum, pageSize), qw));
    }
}