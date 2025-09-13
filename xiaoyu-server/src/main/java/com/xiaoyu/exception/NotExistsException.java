package com.xiaoyu.exception;

public class NotExistsException extends BaseException{
    public NotExistsException() {
        super("不存在");
    }
    public NotExistsException(String message) {
        super(message);
    }
}
