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

package com.datalinkx.driver.dsdriver.esdriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.HttpUtil;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.base.connect.ConnectPool;
import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;


/**
 * Utilities for ElasticSearch
 *
 * Company: www.dtstack.com
 * @author huyifan.zju@163.com
 */
@Slf4j
public class OpenEsService implements EsService {
    private final EsSetupInfo esSetupInfo;
    private final EsDriver esDriver;

    public OpenEsService(EsSetupInfo esSetupInfo, EsDriver esDriver) {
        this.esSetupInfo = esSetupInfo;
        this.esDriver = esDriver;
    }

    public static final Integer ES_VERSION_6 = 6;

    public RestClient getClient() throws Exception {
        List<HttpHost> httpHostList = new ArrayList<>();
        String[] addr = esSetupInfo.getAddress().split(",");
        for (String add : addr) {
            try {
                HttpUtil.HttpMeta httpMeta = HttpUtil.partHttp(add);

                httpHostList.add(new HttpHost(httpMeta.getIp(), httpMeta.getPort(), httpMeta.getSchema()));
            } catch (Exception e) {
                throw new Exception("集群地址格式错误");
            }
        }

        RestClientBuilder builder = RestClient.builder(httpHostList.toArray(new HttpHost[0]));

        if (StringUtils.isNotBlank(esSetupInfo.getUid())) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(esSetupInfo.getUid(),
                    esSetupInfo.getPwd()));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }

        RestClient client = builder.build();
        try {
            getVersion(client);
        } catch (Exception e) {
            throw new Exception("集群连接不通，请检查配置");
        }

        return client;
    }

    public  List<String> getIndexes() throws Exception {
        RestClient restClient = ConnectPool.getConnection(esDriver, RestClient.class);
        try {
            List<String> indices = new ArrayList<>();

//            Response response = restClient.performRequest("GET", "/_cat/indices?v&format=json");
            Response response = restClient.performRequest(new Request("GET", "/_cat/indices?v&format=json"));

            // parse the JSON response
            String rawBody = EntityUtils.toString(response.getEntity());
            List<Map<String, Object>>  list = JsonUtils.toListMap(rawBody);

            // get the index names
            if (list != null) {
                indices = list.stream().map(x -> x.get("index").toString()).collect(Collectors.toList());
            }

            return indices;
        } catch (ResponseException e) {
            throw new Exception(getErrorInfo(EntityUtils.toString(e.getResponse().getEntity())));
        } catch (IOException e) {
            log.error("", e);
            throw e;
        } finally {
            ConnectPool.releaseConnection(esDriver.getConnectId(), restClient);
        }
    }

    public List<TableField> getFields(String tableName) throws Exception {
        RestClient restClient = ConnectPool.getConnection(esDriver, RestClient.class);
        try {
//            Response response = restClient.performRequest();

            Response response = restClient.performRequest(new Request("GET",
                    String.format("/%s/_mappings?format=json", tableName)));

            String rawBody = EntityUtils.toString(response.getEntity());
            JsonNode jsonNode = JsonUtils.toJsonNode(rawBody);

            List<TableField> tableFieldList = new ArrayList<>();
            Integer seqNo = 0;
            Integer version = getVersion(restClient);
            Set<String> fieldExistSet = new HashSet<>();
            if (version <= ES_VERSION_6) {
                for (Iterator<JsonNode> it = jsonNode.get(tableName).get("mappings").elements(); it.hasNext();) {
                    JsonNode docType = it.next();

                    for (Iterator<String> iter = docType.get("properties").fieldNames(); iter.hasNext();) {
                        String fName = iter.next();
                        if (!fieldExistSet.contains(fName)) {
                            String fType = docType.get("properties").get(fName).get("type") == null ? ""
                                    : docType.get("properties").get(fName).get("type").asText();
                            tableFieldList.add(TableField.builder().name(fName).type(fType)
                                    .rawType(fType).remark("").seqNo(seqNo++).uniqIndex(false).build());
                            fieldExistSet.add(fName);
                        }
                    }
                }
            } else {
                JsonNode mappings = jsonNode.get(tableName).get("mappings");
                for (Iterator<String> iter = mappings.get("properties").fieldNames(); iter.hasNext();) {
                    String fName = iter.next();
                    if (!fieldExistSet.contains(fName)) {
                        String fType = mappings.get("properties").get(fName).get("type") == null ? ""
                                : mappings.get("properties").get(fName).get("type").asText();
                        tableFieldList.add(TableField.builder().name(fName).type(fType)
                                .rawType(fType).remark("").seqNo(seqNo++).uniqIndex(false).build());
                        fieldExistSet.add(fName);
                    }
                }
            }

            return tableFieldList;
        } catch (ResponseException e) {
            throw new Exception(getErrorInfo(EntityUtils.toString(e.getResponse().getEntity())));
        } catch (IOException e) {
            log.error("", e);
            throw e;
        } finally {
            ConnectPool.releaseConnection(esDriver.getConnectId(), restClient);
        }
    }

    public Integer getVersion(RestClient restClient) throws Exception {
        try {
            Response response = restClient.performRequest(new Request("GET", "?format=json"));
            String rawBody = EntityUtils.toString(response.getEntity());
            return Integer.parseInt(JsonUtils.toJsonNode(rawBody).get("version").get("number").asText().split("\\.")[0]);
        } catch (IOException e) {
            log.error("", e);
            String message = e.getMessage();
            if (e.getMessage() != null && e.getMessage().contains("unable to authenticate user")) {
                message = "用户认证失败";
            }
            throw new Exception(message);
        }
    }

    public List<String> getIndexType(String tableName) throws Exception {
        RestClient restClient = ConnectPool.getConnection(esDriver, RestClient.class);
        try {
            Response response = null;
            List<String> typeList = new ArrayList<>();
            if (getVersion(restClient) > ES_VERSION_6) {
                return new ArrayList<>();
            }

            response = restClient.performRequest(new Request("GET", String.format("/%s/_mappings?format=json", tableName)));
            String rawBody = EntityUtils.toString(response.getEntity());
            JsonNode jsonNode = JsonUtils.toJsonNode(rawBody);
            Iterator<String> iter = jsonNode.get(tableName).get("mappings").fieldNames();
            while (iter.hasNext()) {
                typeList.add(iter.next());
            }
            return typeList;
        } catch (ResponseException e) {
            throw new Exception(getErrorInfo(EntityUtils.toString(e.getResponse().getEntity())));
        } catch (IOException e) {
            throw e;
        } finally {
            ConnectPool.releaseConnection(esDriver.getConnectId(), restClient);
        }
    }

    public Integer getVersionByClient(RestClient restClient) throws Exception {
        Response response = restClient.performRequest(new Request("GET", "?format=json"));
        String rawBody = EntityUtils.toString(response.getEntity());
        return Integer.parseInt(JsonUtils.toJsonNode(rawBody).get("version").get("number").asText().split("\\.")[0]);
    }

    @Override
    public void checkConnectAlive(Object conn) throws Exception {
        getVersionByClient((RestClient) conn);
    }

    @Override
    public String retrieveMax(String tableName, String json, String maxFieldName) throws Exception {
        RestClient restClient = ConnectPool.getConnection(esDriver, RestClient.class);
        try {
            HttpEntity entity = new NStringEntity(json, ContentType.APPLICATION_JSON);
            Request request = new Request("POST", String.format("/%s/_search?format=json", tableName));
            request.setEntity(entity);

            Response response = restClient.performRequest(request);

            String rawBody = EntityUtils.toString(response.getEntity());
            JsonNode jsonNode = JsonUtils.toJsonNode(rawBody);
            Iterator<JsonNode> iterator = jsonNode.get("hits").get("hits").elements();
            if (iterator.hasNext()) {
                JsonNode data = iterator.next();
                return data.get("_source").get(maxFieldName).asText();
            }

            return null;
        } catch (ResponseException e) {
            throw new Exception(getErrorInfo(EntityUtils.toString(e.getResponse().getEntity())));
        } catch (IOException e) {
            log.error("", e);
            throw e;
        } finally {
            ConnectPool.releaseConnection(esDriver.getConnectId(), restClient);
        }
    }

    @Override
    public void truncateData(String indexName) throws Exception {
        RestClient restClient = ConnectPool.getConnection(esDriver, RestClient.class);
        try {
            HashMap<Object, Object> map = Maps.newHashMap();
            map.put("query", ImmutableMap.of("match_all", Maps.newHashMap()));
            HttpEntity entity = new NStringEntity(JsonUtils.toJson(map), ContentType.APPLICATION_JSON);
            Request request = new Request("POST", String.format("/%s/_delete_by_query", indexName));
            request.setEntity(entity);
            Response response = restClient.performRequest(request);
            if (200 != response.getStatusLine().getStatusCode()) {
                log.error("清除数据失败: {}", EntityUtils.toString(response.getEntity()));
                throw new DatalinkXJobException("ES清楚数据失败!");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            ConnectPool.releaseConnection(esDriver.getConnectId(), restClient);
        }
    }
}
