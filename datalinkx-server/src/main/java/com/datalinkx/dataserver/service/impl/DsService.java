package com.datalinkx.dataserver.service.impl;

import static com.datalinkx.common.utils.IdUtils.genKey;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.datalinkx.driver.dsdriver.esdriver.EsSetupInfo;
import com.datalinkx.driver.dsdriver.mysqldriver.MysqlSetupInfo;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.Base64Utils;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.DsTbBean;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.DsForm;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.DsTbRepository;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;


@Component
@Service
@Log4j2
public class DsService {

	@Autowired
	private DsRepository dsRepository;
	@Autowired
	private DsTbRepository dsTbRepository;

	public Map<Integer, String> genTypeToDbNameMap() {
		Map<Integer, String> typeToDbNameMap = new HashMap<>();
		typeToDbNameMap.put(MetaConstants.DsType.MYSQL, "mysql");
		typeToDbNameMap.put(MetaConstants.DsType.ELASTICSEARCH, "elasticsearch");
		return typeToDbNameMap;
	}


	/**
	* @Description //数据源创建
	* @Date 2021/1/12 6:13 下午
	* @param form
	* @return String dsId
	**/
	@Transactional(rollbackFor = Exception.class)
	@SneakyThrows
	public String create(DsForm.DsCreateForm form) {
		String dsId = genKey("ds");
		// 检测数据源是否重复，重名或者已接入
		DsBean nameCheck = dsRepository.findByName(form.getName());
		if (!ObjectUtils.isEmpty(nameCheck)) {
			throw new DatalinkXServerException(form.getName() + " 数据源名称存在");
		}

		// 获取数据源配置信息
		DsBean dsBean = new DsBean();
		dsBean.setDsId(dsId);
		dsBean.setType(form.getType());
		dsBean.setUsername(form.getUsername());
		dsBean.setHost(form.getHost());
		dsBean.setPort(form.getPort());
		dsBean.setName(form.getName());
		dsBean.setDatabase(form.getDatabase());
		if (form.getPassword() != null) {
			dsBean.setPassword(Base64Utils.encodeBase64(form.getPassword().getBytes(StandardCharsets.UTF_8)));
		}

//		ExportDsForm.ConfigForm config = form.getConfig();
//		if (!ObjectUtils.isEmpty(config)) {
//			if (StringUtils.isEmpty(config.getSchema())) {
//				dsBean.setSchema(config.getSchema());
//			}
//		}
//
//		dsBean.setConfig(JsonUtils.toJson(config));

		checkConnect(dsBean);

		//获取选中的表并创建
		List<String> tbNameList = ObjectUtils.isEmpty(form.getTbNameList()) ? new ArrayList<>() : form.getTbNameList();
		for (String tbName : tbNameList) {
			xtbCreate(tbName, dsId);
		}

		dsRepository.save(dsBean);

		return dsId;
	}

	public String xtbCreate(String tbName, String dsId) {
		String xtbId = genKey("xtb");
		DsTbBean dsTbBean = new DsTbBean();
		dsTbBean.setTbId(xtbId);
		dsTbBean.setName(tbName);
		dsTbBean.setDsId(dsId);
		dsTbRepository.save(dsTbBean);
		return xtbId;
	}


	private void checkConnect(DsBean dsBean) {
		try {
			IDsReader ignored = DsDriverFactory.getDsReader(getConnectId(dsBean));
			ignored.connect(true);
			log.info("connect success");
		} catch (Exception e) {
			log.error("connect error", e);
			throw new DatalinkXServerException(e.getMessage());
		}
	}

	public String getConnectId(DsBean dsBean) {
		String toType = Optional.ofNullable(genTypeToDbNameMap().get(dsBean.getType())).orElse("").toLowerCase();
		switch (toType) {
			case "mysql":
				MysqlSetupInfo mysqlSetupInfo = new MysqlSetupInfo();
				mysqlSetupInfo.setServer(dsBean.getHost());
				mysqlSetupInfo.setPort(dsBean.getPort());
				mysqlSetupInfo.setType(toType);
				mysqlSetupInfo.setUid(dsBean.getUsername());
				mysqlSetupInfo.setPwd(dsBean.getPassword());
				mysqlSetupInfo.setIsExport(1);
				mysqlSetupInfo.setCrypter(false);
				return ConnectIdUtils.encodeConnectId(JsonUtils.toJson(mysqlSetupInfo));
			case "elasticsearch":
				EsSetupInfo esSetupInfo = new EsSetupInfo();
				esSetupInfo.setType(toType);
				esSetupInfo.setAddress(dsBean.getHost() + ":" + dsBean.getPort());
				esSetupInfo.setPwd(dsBean.getPassword());
				esSetupInfo.setUid(dsBean.getUsername());
				return ConnectIdUtils.encodeConnectId(JsonUtils.toJson(esSetupInfo));
			default:
				Map<String, Object> map = new HashMap<>();
				map.put("type", genTypeToDbNameMap().get(dsBean.getType()));
				return ConnectIdUtils.encodeConnectId(JsonUtils.toJson(map));
		}
	}


    public PageVo<List<DsBean>> dsPage(DsForm.DataSourcePageForm dataSourcePageForm) {
		PageRequest pageRequest = PageRequest.of(dataSourcePageForm.getPageNo() - 1, dataSourcePageForm.getPageSize());
		Page<DsBean> dsBeans = dsRepository.pageQuery(pageRequest, dataSourcePageForm.getName());
		PageVo<List<DsBean>> result = new PageVo<>();
		result.setPageNo(dataSourcePageForm.getPageNo());
		result.setPageSize(dataSourcePageForm.getPageSize());
		result.setData(dsBeans.getContent());
		result.setTotalPage(dsBeans.getTotalPages());
		result.setTotal(dsBeans.getTotalElements());
		return result;
	}

	public void del(String dsId) {
		dsRepository.deleteByDsId(dsId);
	}

	public DsBean info(String dsId) {
		return dsRepository.findByDsId(dsId)
				.map(dsBean -> {
					if (!ObjectUtils.isEmpty(dsBean.getPassword())) {
						String pwd = null;
						try {
							pwd = new String(Base64Utils.decodeBase64(dsBean.getPassword()));
						} catch (UnsupportedEncodingException e) {
							log.error("ds密码解析失败");
						}
						dsBean.setPassword(pwd);
					}
					return dsBean;
				})
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "from ds not exist"));
	}

	public void modify(DsForm.DsCreateForm form) {
		Optional<DsBean> dsCheck = dsRepository.findByDsId(form.getDsId());
		DsBean dsBean = dsCheck.orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "ds not exist"));
		dsBean.setUsername(form.getUsername());
		dsBean.setHost(form.getHost());
		dsBean.setPort(form.getPort());
		dsBean.setName(form.getName());
		dsBean.setDatabase(form.getDatabase());
		dsRepository.save(dsBean);
	}

	@SneakyThrows
	public List<String> fetchTables(String dsId) {
		DsBean dsBean = dsRepository.findByDsId(dsId).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS));
		List<String> tableList = new ArrayList<>();
		try {
			IDsReader dsReader = DsDriverFactory.getDsReader(getConnectId(dsBean));
			tableList = dsReader.treeTable(dsBean.getDatabase(), "").stream().map(DbTree::getName).collect(Collectors.toList());
		} catch (Exception e) {
			log.error("connect error", e);
			throw new DatalinkXServerException(e);
		}
		return tableList;
	}

	public List<DsBean> list() {
		return dsRepository.findAllByIsDel(0);
	}


	public List<TableField> fetchFields(String dsId, String tbName) {
		DsBean dsBean = dsRepository.findByDsId(dsId).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS));
		try {
			IDsReader dsReader = DsDriverFactory.getDsReader(getConnectId(dsBean));
			return dsReader.getFields(dsBean.getDatabase(), "", tbName);
		} catch (Exception e) {
			log.error("connect error", e);
			throw new DatalinkXServerException(e);
		}
	}
}
