package com.datalinkx.dataserver.bean.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthUserBody {
    // Getters and Setters
    private String roleId;
    private String[] userIds;

}