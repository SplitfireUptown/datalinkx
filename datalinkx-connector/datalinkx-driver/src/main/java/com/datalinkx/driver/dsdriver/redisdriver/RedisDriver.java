package com.datalinkx.driver.dsdriver.redisdriver;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.datalinkx.driver.dsdriver.base.writer.WriterInfo;

public class RedisDriver implements AbstractDriver<RedisSetupInfo, AbstractReader, RedisWriter>, IDsWriter {

    private final String connectId;
    private final RedisSetupInfo redisSetupInfo;
    private final RedisService redisService;

    public RedisDriver(String connectId) {
        this.connectId = connectId;
        this.redisSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId) , RedisSetupInfo.class);
        this.redisSetupInfo.setPwd(rebuildPassword(redisSetupInfo.getPwd()));
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
    public void truncateData(FlinkActionMeta param) throws Exception {

    }

    @Override
    public Object getWriterInfo(FlinkActionMeta param) throws Exception {
        String tableName = param.getWriter().getTableName();
        String[] typeKeyArray = tableName.split(MetaConstants.DsType.REDIS_SPIT_STR);
        String typeArray = typeKeyArray[0];
        String key = typeKeyArray[1];

        String[] typeModeArray = typeArray.split("-");
        String type = typeModeArray[0];
        String mode;
        if (typeModeArray.length > 1) {
            mode = typeModeArray[1];
        } else {
            mode = "set";
        }

        WriterInfo<RedisWriter> writerInfo = new WriterInfo<>();
        writerInfo.setName("rediswriter");
        writerInfo.setParameter(RedisWriter.builder()
                        .customKey(key)
                        .password(redisSetupInfo.getPwd())
                        .hostPort(redisSetupInfo.getHost() + ":" + redisSetupInfo.getPort())
                        .mode(mode)
                        .type(type)
                        .database(redisSetupInfo.getDatabase())
                        // 只是为了补充JobGraph信息，无异议
                        .keyIndexes(new Integer[] {0, 1})
                .build());
        return writerInfo;
    }
}
