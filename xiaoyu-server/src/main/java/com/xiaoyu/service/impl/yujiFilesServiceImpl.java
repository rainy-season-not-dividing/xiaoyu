package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.FilesPO;
import com.xiaoyu.mapper.yujiFilesMapper;
import com.xiaoyu.service.yujiFilesService;
import com.xiaoyu.util.AliOssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static com.google.common.io.Files.getFileExtension;

@Slf4j
@Service
public class yujiFilesServiceImpl extends ServiceImpl<yujiFilesMapper, FilesPO> implements yujiFilesService {

    private final AliOssUtil ossUtil;

    @Resource
    private yujiFilesMapper fileMapper;

    public yujiFilesServiceImpl(AliOssUtil ossUtil) {
        this.ossUtil = ossUtil;
    }

    @Override
    public Map<String, Object> uploadFile(MultipartFile file, String bizType, Long userId) {
        log.info("开始上传文件，用户ID: {}, 业务类型: {}, 文件名: {}", userId, bizType, file.getOriginalFilename());

        String objectName = null;
        String fileUrl = null;
        try {
            // 解析并校验业务类型
            FilesPO.BizType bizTypeEnum = parseBizType(bizType);

            // 生成对象名称
            objectName = generateObjectName(file.getOriginalFilename(), bizTypeEnum.name());

            // 上传文件到OSS
            fileUrl = ossUtil.upload(file.getBytes(), objectName);


            // 保存文件信息到数据库
            FilesPO filePO = new FilesPO();
            filePO.setUserId(userId);
            filePO.setBizType(bizTypeEnum);
            filePO.setFileUrl(fileUrl);
            filePO.setSize((int) file.getSize());
            filePO.setCreatedAt(LocalDateTime.now());
            save(filePO);

            log.info("文件上传成功，文件ID: {}", filePO.getId());
            return Map.of("fileId", filePO.getId(), "fileUrl", fileUrl);

        } catch (Exception e) {
            // 如果OSS已上传但后续失败，执行回滚删除
            if (fileUrl != null && objectName != null) {
                try {
                    ossUtil.deleteFile(objectName);
                    log.warn("已回滚删除OSS文件: {}", objectName);
                } catch (Exception ex) {
                    log.warn("回滚删除OSS文件失败: {} - {}", objectName, ex.getMessage());
                }
            }
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }


    /**
     * 将传入的业务类型字符串解析为枚举并校验
     */
    private FilesPO.BizType parseBizType(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        try {
            return FilesPO.BizType.valueOf(bizType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("不支持的业务类型: " + bizType);
        }
    }

    /**
     * 生成对象名称
     *
     * @param originalFilename 原始文件名
     * @param bizType 业务类型
     * @return 对象名称
     */
    private String generateObjectName(String originalFilename, String bizType) {
        // 解析并使用规范化的业务类型作为路径
        FilesPO.BizType bizTypeEnum = parseBizType(bizType);

        String extension = getFileExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new IllegalArgumentException("无法识别文件扩展名: " + originalFilename);
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String datePath = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        return String.format("%s/%s/%s.%s", bizTypeEnum.name().toLowerCase(), datePath, uuid, extension);
    }




}
