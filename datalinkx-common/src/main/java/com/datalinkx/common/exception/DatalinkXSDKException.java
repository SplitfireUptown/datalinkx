package com.datalinkx.common.exception;

/**
 * @author: uptown
 * @date: 2025/4/3 22:28
 */
public class DatalinkXSDKException extends RuntimeException {

    private static final long serialVersionUID = -940285811464169752L;


    public DatalinkXSDKException(String msg) {
        super(msg);
    }

    public DatalinkXSDKException(Throwable throwable) {
        super(throwable);
    }
}
