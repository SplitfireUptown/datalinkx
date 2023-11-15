package com.datalinkx.driver.dsdriver.esdriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsSetupInfo extends SetupInfo {
    private String address;
    private String version;
    private String uid;
    private String pwd;

    @JsonProperty("kerberos_user")
    private String kerberosUser;

    @JsonProperty("kerberos_krb5_path")
    private String kerberosKrb5Path;

    @JsonProperty("kerberos_keytab_path")
    private String kerberosKeytabPath;

    @JsonProperty("kerberos_jaas_path")
    private String kerberosJaasPath;
}
