package com.datalinkx.common.result;

import lombok.Getter;

public enum StatusCode {
    API_INTERNAL_ERROR(500),
    DS_NOT_EXISTS(101),
    DS_CONFIG_ERROR(102),
    DS_HAS_JOB_DEPEND(102),

    JOB_IS_RUNNING(1001),
    JOB_NOT_EXISTS(1000),
    JOB_CONFIG_ERROR(1003),


    DRIVER_LOAD_FAIL(1111),
    ALARM_CONFIG_NOT_EXISTS(1500),

    XTB_NOT_EXISTS(2000);

    private final int value;

    @Getter
    private String msg;


    StatusCode(int code) {
        this.value = code;
    }

    StatusCode(int code, String msg) {
        this.value = code;
        this.msg = msg;
    }


    public int getValue() {
        return this.value;
    }

    public static StatusCode getByCode(int code) {
        for (StatusCode enums : StatusCode.values()) {
            if (enums.getValue() == code) {
                return enums;
            }
        }
        return null;
    }

	public static Integer getCodeByName(String name) {
    	try {
			return StatusCode.valueOf(name).getValue();
		} catch (IllegalArgumentException e) {
    		return null;
		}

	}

}
