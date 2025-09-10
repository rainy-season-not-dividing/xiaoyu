package com.xiaoyu.exception;

public class AlreadyBeFriendException extends BaseException{
    public AlreadyBeFriendException() {
        super("已经是好友");
    }

    public AlreadyBeFriendException(String message) {
        super(message);
    }
}
