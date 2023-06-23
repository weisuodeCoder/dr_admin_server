package com.darhan.handler;

import com.darhan.entity.ResultCode;
import lombok.Data;

@Data
public class CustomException extends Exception{
    private ResultCode resultCode;
    public CustomException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }
}
