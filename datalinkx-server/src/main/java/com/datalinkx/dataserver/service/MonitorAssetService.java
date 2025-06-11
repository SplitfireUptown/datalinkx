package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.bean.vo.MonitorVo;

import java.util.List;

public interface MonitorAssetService {

     MonitorVo.AssetTotalMonitorVo assetTotal();

     MonitorVo.AssetDsJobGroupVo assetDsJobGroupVo();

     List<MonitorVo.ItemCountVo> assetJobStatus();
}
