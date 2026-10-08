package com.example.MiaoShaSystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.MiaoShaSystem.annotation.OpLog;
import com.example.MiaoShaSystem.common.BizException;
import com.example.MiaoShaSystem.common.Result;
import com.example.MiaoShaSystem.dto.MerchantAuditDTO;
import com.example.MiaoShaSystem.entity.User;
import com.example.MiaoShaSystem.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/merchant")
public class AdminMerchantController {

    @Autowired
    private IUserService userService;

    /** 商家列表 */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Byte auditStatus,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<User>()
                .eq(User::getRole, "MERCHANT")
                .eq(auditStatus != null, User::getAuditStatus, auditStatus)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getShopName, keyword))
                .orderByDesc(User::getId);

        IPage<User> page = userService.page(new Page<>(pageNum, pageSize), qw);
        return Result.success(page);
    }

    /** 审核商家 */
    @PutMapping("/{id}/audit")
    @OpLog(action = "AUDIT_MERCHANT", targetType = "MERCHANT")
    public Result audit(@PathVariable Long id, @RequestBody MerchantAuditDTO dto) {
        if (dto.getStatus() == null || (dto.getStatus() != 1 && dto.getStatus() != 2)) {
            throw new BizException("审核状态不合法");
        }

        User merchant = userService.getById(id);
        if (merchant == null) {
            throw new BizException("商家不存在");
        }
        if (!"MERCHANT".equals(merchant.getRole())) {
            throw new BizException("不是商家账号");
        }

        merchant.setAuditStatus(dto.getStatus());
        merchant.setAuditRemark(dto.getRemark());
        userService.updateById(merchant);

        return Result.success(dto.getStatus() == 1 ? "已通过" : "已拒绝", null);
    }
}