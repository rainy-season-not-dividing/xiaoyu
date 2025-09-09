package com.xiaoyu.handler;


import com.xiaoyu.exception.BaseException;
import com.xiaoyu.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler   // 这个注解的作用是，拦截所有继承自BaseException的异常，函数名可以随意，只要返回值是Result类型即可
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}",ex.getMessage());
        return Result.error(ex.getMessage());
    }
}
