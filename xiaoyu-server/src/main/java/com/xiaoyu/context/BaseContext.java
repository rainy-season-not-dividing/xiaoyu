package com.xiaoyu.context;



public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long userId){
        threadLocal.set(userId);
    }

    public static Long getId(){
        return threadLocal.get();
    }

    public static void clear(){
        threadLocal.remove();
    }


}
