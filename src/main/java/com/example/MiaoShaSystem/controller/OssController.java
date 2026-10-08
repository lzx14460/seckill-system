package com.example.MiaoShaSystem.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.example.MiaoShaSystem.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/oss")
public class OssController {

    @Value("${aliyun.oss.region}")
    private String region;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    @Value("${aliyun.oss.role-arn}")
    private String roleArn;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @GetMapping("/sts-token")
    public Result stsToken() throws ClientException {
        // 1. 初始化 profile（用 RAM 用户 AK/SK）
        DefaultProfile profile = DefaultProfile.getProfile(
                region, accessKeyId, accessKeySecret
        );
        IAcsClient client = new DefaultAcsClient(profile);

        // 2. 构造 AssumeRole 请求
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setRoleArn(roleArn);
        request.setRoleSessionName("seckill-upload");
        request.setDurationSeconds(900L);   // 15 分钟

        // 3. 请求 STS
        AssumeRoleResponse response = client.getAcsResponse(request);
        AssumeRoleResponse.Credentials creds = response.getCredentials();

        // 4. 返回临时凭证
        Map<String, String> data = new HashMap<>();
        data.put("accessKeyId", creds.getAccessKeyId());
        data.put("accessKeySecret", creds.getAccessKeySecret());
        data.put("securityToken", creds.getSecurityToken());
        data.put("region", region);
        data.put("bucket", bucket);

        return Result.success(data);
    }
}