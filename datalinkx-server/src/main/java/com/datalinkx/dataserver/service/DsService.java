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
 * 业务数据源管理service
 */
public interface DsService {
    /**
     * 数据源创建
     */
    String create(DsForm.DsCreateForm form);

    /**
     * 数据源加密信息
     */
    String getConnectId(DsBean dsBean);

    /**
     * 分页查询
     */
    PageVo<List<DsBean>> dsPage(DsForm.DataSourcePageForm dataSourcePageForm);

    /**
     * 数据源删除
     */
    void del(String dsId);

    /**
     * 数据源详情
     */
    DsBean info(String dsId);

    /**
     * 数据源编辑
     */
    void modify(DsForm.DsCreateForm form);

    /**
     * 指定数据源下的数据表
     */
    List<String> fetchTables(String dsId);
    /**
     * 列表查询
     */
    List<DsBean> list();
    /**
     * 指定数据源-数据表下的字段列表
     */
    List<DbTableField> fetchFields(String dsId, String tbName);
}
