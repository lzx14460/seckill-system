package com.example.MiaoShaSystem.common;

import lombok.Data;

@Data
public class Result {
    private Integer code;
    private String message;
    private Object data;
    public static Result success(Object data){
        Result result = new Result();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }
    public static  Result success(String message,Object data){
        Result result = new Result();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }
    public static Result fail(ResultCode resultCode) {
        Result result = new Result();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());

        return result;
    }
    public static Result fail(String message) {
        Result r = new Result();
        r.setCode(ResultCode.FAIL.getCode());
        r.setMessage(message);
        return r;
    }
}
