package com.xiaoyu.exception;

public class BadRequestException extends BaseException{
    public BadRequestException() {
        super();
    }
    public BadRequestException(String message) {
        super(message);
    }
}
