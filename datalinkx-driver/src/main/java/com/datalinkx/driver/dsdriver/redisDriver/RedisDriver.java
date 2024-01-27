package com.datalinkx.driver.dsdriver.redisDriver;

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.TelnetUtil;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.datalinkx.driver.dsdriver.base.writer.WriterInfo;

public class RedisDriver implements AbstractDriver<RedisSetupInfo, AbstractReader, RedisWriter>, IDsWriter {

    private final String connectId;
    private final RedisSetupInfo redisSetupInfo;
    private final RedisService redisService;

    public RedisDriver(String connectId) {
        this.connectId = connectId;
        this.redisSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId) , RedisSetupInfo.class);
        this.redisService = new RedisService(this.redisSetupInfo);
    }

    @Override
    public Object connect(boolean check) throws Exception {
        return this.redisService.getClient();
    }

    @Override
    public String getConnectId() {
        return connectId;
    }

    @Override
    public void checkConnectAlive(Object conn) throws Exception {
        String host = this.redisSetupInfo.getHost();
        Integer port = this.redisSetupInfo.getPort();
        TelnetUtil.telnet(host, port);
    }

    @Override
    public void truncateData(FlinkActionParam param) throws Exception {

    }

    @Override
    public Object getWriterInfo(FlinkActionParam param) throws Exception {
        String tableName = param.getWriter().getTableName();
        WriterInfo<RedisWriter> writerInfo = new WriterInfo<>();
        writerInfo.setName("rediswriter");
        writerInfo.setParameter(RedisWriter.builder()
                        .customKey(redisSetupInfo.getCustomKey())
                        .password(redisSetupInfo.getPwd())
                        .hostPort(redisSetupInfo.getHost() + ":" + redisSetupInfo.getPort())
                        .mode(redisSetupInfo.getMode())
                        .type(redisSetupInfo.getType())
                        .database(redisSetupInfo.getDatabase())
                        .keyIndexes(new String[0])
                .build());
        return writerInfo;
    }

    @Override
    public void afterWrite(FlinkActionParam param) {

    }
}
