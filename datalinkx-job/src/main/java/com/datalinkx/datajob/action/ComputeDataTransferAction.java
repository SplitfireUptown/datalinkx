package com.datalinkx.datajob.action;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataclient.client.seatunnel.SeaTunnelClient;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.SeatunnelActionMeta;
import com.datalinkx.driver.dsdriver.base.model.StreamFlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;



@Slf4j
@Component
@ConditionalOnBean(SeaTunnelClient.class)
public class ComputeDataTransferAction extends AbstractDataTransferAction<DataTransJobDetail, SeatunnelActionMeta> {
    public static ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Autowired
    SeaTunnelClient seaTunnelClient;
    @Autowired
    DatalinkXServerClient datalinkXServerClient;

    @Override
    protected void begin(DataTransJobDetail info) {

    }

    @Override
    protected void end(SeatunnelActionMeta unit, int status, String errmsg) {

    }

    @Override
    protected void beforeExec(SeatunnelActionMeta unit) throws Exception {

    }

    @Override
    protected void execute(SeatunnelActionMeta unit) throws Exception {

    }

    @Override
    protected boolean checkResult(SeatunnelActionMeta unit) {
        return false;
    }

    @Override
    protected void afterExec(SeatunnelActionMeta unit, boolean success) {

    }

    @Override
    protected SeatunnelActionMeta convertExecUnit(DataTransJobDetail info) throws Exception {
        return null;
    }
}
