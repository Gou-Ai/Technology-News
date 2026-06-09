package com.compuspulse.data.remote.model;

public class ApiResponse<T> {
    private T data;
    private int errorCode;
    private String errorMsg;

    public T getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public boolean isSuccess() {
        return errorCode == 0;
    }
}
