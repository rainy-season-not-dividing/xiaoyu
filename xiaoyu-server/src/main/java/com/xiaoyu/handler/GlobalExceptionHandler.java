package com.xiaoyu.handler;


import com.xiaoyu.exception.BaseException;
import com.xiaoyu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler   // 这个注解的作用是，拦截所有继承自BaseException的异常，函数名可以随意，只要返回值是Result类型即可
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}",ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /** 专门抓上传大小超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        String msg = "文件大小超出限制，单文件最大 10 MB";
        log.warn("上传文件过大：{}", e.getMessage());
        return Result.error(msg);
    }
}
