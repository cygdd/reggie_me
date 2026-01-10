package com.itheima.reggie.common;

/**
 * 自定义业务异常类
 */
public class CustomException extends RuntimeException {
    /**
     * 自定义业务异常类
     * 注意：自定义异常都需要继承RuntimeException
     */
    public CustomException(String message){
        super(message);
    }
}
