package com.datalinkx.datajob.action;

import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.StreamFlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: uptown
 * @date: 2024/4/27 14:23
 */
@Slf4j
@Component
public class StreamDataTransferAction extends AbstractDataTransferAction<DataTransJobDetail, StreamFlinkActionMeta> {
    @Override
    protected void begin(DataTransJobDetail info) {
        // 修改任务状态
    }

    @Override
    protected void end(DataTransJobDetail info, int status, String errmsg) {
        // 修改任务状态
    }

    @Override
    protected void beforeExec(StreamFlinkActionMeta unit) throws Exception {

    }

    @Override
    protected void execute(StreamFlinkActionMeta unit) throws Exception {

    }

    @Override
    protected boolean checkResult(StreamFlinkActionMeta unit) {
        return false;
    }

    @Override
    protected void afterExec(StreamFlinkActionMeta unit, boolean success) {
        // 记录checkpoint
    }

    @Override
    protected StreamFlinkActionMeta convertExecUnit(DataTransJobDetail info) {
        return null;
    }
}
