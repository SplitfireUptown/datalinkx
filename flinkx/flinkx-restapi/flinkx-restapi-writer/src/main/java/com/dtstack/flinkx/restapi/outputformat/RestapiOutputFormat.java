/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.flinkx.restapi.outputformat;

import com.dtstack.flinkx.exception.WriteRecordException;
import com.dtstack.flinkx.outputformat.BaseRichOutputFormat;
import com.dtstack.flinkx.restapi.common.HttpUtil;
import com.dtstack.flinkx.util.ExceptionUtil;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.flink.types.Row;
import org.apache.flink.util.CollectionUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.dtstack.flinkx.restapi.common.RestapiKeys.KEY_BATCH;

/**
 * @author : tiezhu
 * @date : 2020/3/12
 * 当前只考虑了元数据读取，和带有字段名column读取的情况，其他情况暂未考虑
 */
public class RestapiOutputFormat extends BaseRichOutputFormat {

    protected String url;

    protected String method;

    protected ArrayList<String> column;

    protected Map<String, Object> params;

    protected Map<String, Object> body;

    protected Map<String, String> header;

    protected static final int DEFAULT_TIME_OUT = 300000;

    protected Gson gson;

    @Override
    protected void openInternal(int taskNumber, int numTasks) throws IOException {
//        params.put("threadId", UUID.randomUUID().toString().substring(0, 8));
        gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    protected void writeSingleRecordInternal(Row row) throws WriteRecordException {
        LOG.info("start write single record");
        CloseableHttpClient httpClient = HttpUtil.getHttpClient();
        int index = 0;
        List<Object> dataRow = new ArrayList<>();
        try {
            dataRow.add(getDataForMobius(row, column));
            URIBuilder uriBuilder = new URIBuilder(this.url);
            if (!CollectionUtil.isNullOrEmpty(params)) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue().toString());
                }
            }
            sendRequest(httpClient, gson.toJson(row), method, header, uriBuilder.build().toString());
        } catch (Exception e) {
            requestErrorMessage(e, index, row);
            LOG.error(ExceptionUtil.getErrorMessage(e));
            throw new RuntimeException(e);
        } finally {
            // 最后不管发送是否成功，都要关闭client
            HttpUtil.closeClient(httpClient);
        }
    }

    @Override
    protected void writeMultipleRecordsInternal() throws Exception {
        LOG.info("start write multiple records");
        try {
            CloseableHttpClient httpClient = HttpUtil.getHttpClient();
            List<Object> dataRow = new ArrayList<>();
            for (Row row : rows) {
                dataRow.add(getDataForMobius(row, column));
            }
            URIBuilder uriBuilder = new URIBuilder(this.url);
            if (!CollectionUtil.isNullOrEmpty(params)) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    uriBuilder.addParameter(entry.getKey(), entry.getValue().toString());
                }
            }
            LOG.info("this batch size = {}", rows.size());
            sendRequest(httpClient, gson.toJson(dataRow), method, header, uriBuilder.build().toString());
        } catch (Exception e) {
            LOG.error(ExceptionUtil.getErrorMessage(e));
            throw new RuntimeException(e);
        }
    }

    private void requestErrorMessage(Exception e, int index, Row row) {
        if (index < row.getArity()) {
            recordConvertDetailErrorMessage(index, row);
            LOG.warn("添加脏数据:" + row.getField(index));
        }
    }

    private Object getDataFromRow(Row row, List<String> column) throws IOException {
        Map<String, Object> columnData = Maps.newHashMap();
        int index = 0;
        if (!column.isEmpty()) {
            // 如果column不为空，那么将数据和字段名一一对应
            for (; index < row.getArity(); index++) {
                Object value = null;
                if (row.getField(index) instanceof Timestamp) {
                    // 不加不行啊，不加多个毫秒
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    value = simpleDateFormat.format(row.getField(index));
                } else {
                    value = row.getField(index);
                }
                columnData.put(column.get(index), value);
            }
            return gson.toJson(columnData);
        } else {
            return row.getField(index);
        }
    }

    private Object getDataForMobius(Row row, List<String> column) throws IOException {
        List<Object> datas = new ArrayList<>();
        int index = 0;
        if (!column.isEmpty()) {
            // 如果column不为空，那么将数据和字段名一一对应
            for (; index < row.getArity(); index++) {
                Object value = null;
                if (row.getField(index) instanceof Timestamp) {
                    // 不加不行啊，不加多个毫秒
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    value = simpleDateFormat.format(row.getField(index));
                } else {
                    value = row.getField(index);
                }
                datas.add(value);
            }
            return datas;
        } else {
            return row.getField(index);
        }
    }


    private void sendRequest(CloseableHttpClient httpClient,
                             String data,
                             String method,
                             Map<String, String> header,
                             String url) throws IOException {
        LOG.debug("send data:{}", data);
        LOG.info("请求的url：" + url);
        HttpRequestBase request = HttpUtil.getRequest(method, data, header, url);
        //设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_TIME_OUT).setConnectionRequestTimeout(DEFAULT_TIME_OUT)
                .setSocketTimeout(DEFAULT_TIME_OUT).build();
        request.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = httpClient.execute(request);
        LOG.info("响应码：" + httpResponse.getStatusLine().getStatusCode());
        // 重试之后返回状态码不为200
        if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            LOG.warn("重试之后当前请求状态码为" + httpResponse.getStatusLine().getStatusCode());
        } else {
            String responseString = EntityUtils.toString(httpResponse.getEntity());
            LOG.info("响应结果：" + responseString);
        }
    }
}
