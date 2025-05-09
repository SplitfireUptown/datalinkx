package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.DsForm;
import com.datalinkx.driver.dsdriver.base.meta.DbTableField;

import java.util.List;

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
