package com.datalinkx.dataserver.service;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.Base64Utils;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.DsForm;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsDriver;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.esdriver.EsSetupInfo;
import com.datalinkx.driver.dsdriver.mysqldriver.MysqlSetupInfo;
import com.datalinkx.driver.dsdriver.oracledriver.OracleSetupInfo;
import com.datalinkx.driver.dsdriver.redisdriver.RedisSetupInfo;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.datalinkx.common.utils.IdUtils.genKey;

/**
 * @author: uptown
 * @date: 2024/4/25 22:48
 */
public interface DsService {
    String create(DsForm.DsCreateForm form);
    String getConnectId(DsBean dsBean);
    PageVo<List<DsBean>> dsPage(DsForm.DataSourcePageForm dataSourcePageForm);
    void del(String dsId);
    DsBean info(String dsId);
    void modify(DsForm.DsCreateForm form);
    List<String> fetchTables(String dsId);
    List<DsBean> list();
    List<DbTableField> fetchFields(String dsId, String tbName);
}
