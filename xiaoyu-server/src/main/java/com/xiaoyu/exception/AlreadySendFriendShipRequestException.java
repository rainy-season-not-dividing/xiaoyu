package com.xiaoyu.exception;

public class AlreadySendFriendShipRequestException extends BaseException{

    public AlreadySendFriendShipRequestException()
    {
        super("已发送过好友请求");
    }

    public AlreadySendFriendShipRequestException(String message)
    {
        super(message);
    }
}
