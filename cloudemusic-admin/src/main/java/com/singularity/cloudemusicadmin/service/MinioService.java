package com.singularity.cloudemusicadmin.service;

import com.singularity.cloudemusicadmin.common.BusinessException;
import com.singularity.cloudemusicadmin.config.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 上传头像文件到 MinIO，返回可公开访问的 URL
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        String bucket = minioConfig.getAvatarBucket();
        ensureBucketExists(bucket);

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "仅支持图片文件");
        }

        // 提取扩展名
        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "avatar.jpg");
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot);
        }

        // 生成唯一文件名
        String objectName = "avatars/" + userId + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(contentType)
                    .build());

            // 构造公开访问 URL（path-style）
            String endpoint = minioConfig.getEndpoint().replaceAll("/$", "");
            return endpoint + "/" + bucket + "/" + objectName;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new BusinessException(500, "头像上传失败");
        }
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                // 设置为公开读
                String policy = """
                        {
                          "Version": "2012-10-17",
                          "Statement": [{
                            "Effect": "Allow",
                            "Principal": {"AWS": ["*"]},
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                          }]
                        }
                        """.formatted(bucket);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucket)
                        .config(policy)
                        .build());
                log.info("创建 MinIO 公开桶: {}", bucket);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("检查/创建 MinIO 桶失败", e);
            throw new BusinessException(500, "存储服务异常");
        }
    }
}
