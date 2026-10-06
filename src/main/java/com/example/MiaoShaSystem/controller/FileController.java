package com.example.MiaoShaSystem.controller;

import com.example.MiaoShaSystem.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-prefix}")
    private String accessPrefix;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.fail("文件为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只允许上传图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("图片不能超过 5MB");
        }

        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());

        // ★★★ 关键：用绝对路径 ★★★
        File baseDir = new File(System.getProperty("user.dir"), uploadDir);
        File targetDir = new File(baseDir, dateDir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File dest = new File(targetDir, filename);
        file.transferTo(dest.getAbsoluteFile());   // ← 传绝对路径

        String url = accessPrefix + dateDir + "/" + filename;
        return Result.success(url);
    }
}