package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.entity.FilesPO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FilesService extends IService<FilesPO> {
    Map<String, Object> uploadFile(MultipartFile file, String bizType, Long userId);
}
