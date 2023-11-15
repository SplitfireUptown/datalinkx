package com.dtstack.flinkx.datahub;

public enum DatahubVersion {
    datahub10("1.0");
    String code;

    DatahubVersion(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
