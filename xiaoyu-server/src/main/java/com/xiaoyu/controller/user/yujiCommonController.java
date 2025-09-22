package com.xiaoyu.controller.user;

import com.xiaoyu.result.Result;
import com.xiaoyu.util.AliOssUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
public class yujiCommonController {

    @Resource(name="aliOssUtil_yuji")
    private AliOssUtil aliOssUtil;

//    @PostMapping("files/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file);

        try {
            // 原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取原始文件名的后缀   dfdfdf.png
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 构建新文件的名称
            String objectName = UUID.randomUUID().toString() + extension;

            //文件请求路径
            String  fileUrl = aliOssUtil.upload(file.getBytes(), file.getOriginalFilename());
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
        }
        return null;
    }


}
