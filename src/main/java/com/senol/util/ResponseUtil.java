package com.senol.util;

import lombok.Data;

@Data
public class ResponseUtil<T> {
    private Integer code;
    private String message;
    private T data;
    
    public ResponseUtil() {}
    
    public ResponseUtil(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public static <T> ResponseUtil<T> success(T data) {
        return new ResponseUtil<>(200, "成功", data);
    }
    
    public static <T> ResponseUtil<T> success(String message, T data) {
        return new ResponseUtil<>(200, message, data);
    }
    
    public static <T> ResponseUtil<T> error(String message) {
        return new ResponseUtil<>(500, message, null);
    }
    
    public static <T> ResponseUtil<T> error(Integer code, String message) {
        return new ResponseUtil<>(code, message, null);
    }
}
