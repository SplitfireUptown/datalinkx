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

package com.datalinkx.esdriver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datalinkx.common.utils.DateUtil;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.base.meta.DbTableField;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * Utilities for ElasticSearch
 *
 * Company: www.dtstack.com
 * @author huyifan.zju@163.com
 */
public interface EsService {
    Object getClient() throws Exception;

    List<String> getIndexes() throws Exception;

    List<DbTableField> getFields(String tableName) throws Exception;

    List<String> getIndexType(String tableName) throws Exception;

    String retrieveMax(String tableName, String json, String maxFieldName) throws Exception;

    void truncateData(String indexName) throws Exception;

    default Map<String, Object> buildRange(String name, String value,
                                           String op, String mustOrShould, String fieldType) throws Exception {
        String from = null;
        String to = null;
        Boolean includeLower = false;
        Boolean includeUpper = false;
        if (">".equals(op) || ">=".equals(op)) {
            from = value;
            if (">=".equals(op)) {
                includeLower = true;
            }
        } else {
            to = value;
            if ("<=".equals(op)) {
                includeUpper = true;
            }
        }
        String finalFrom = from;
        String finalTo = to;
        Boolean finalIncludeLower = includeLower;
        Boolean finalIncludeUpper = includeUpper;
        Map<String, Object> boolMap = new HashMap<>();
        boolMap.put("bool", new HashMap<String, Object>() {
            {
                put(mustOrShould, new HashMap<String, Object>() {{
                    put("range", new HashMap<String, Object>() {{
                        put(name, new HashMap<String, Object>() {{
                            put("from", finalFrom);
                            put("to", finalTo);
                            put("include_lower", finalIncludeLower);
                            put("include_upper", finalIncludeUpper);
                            if ("date".equals(fieldType)) {
                                if (DateUtil.isFormatted(value, "yyyy-MM-dd HH:mm:ss")) {
                                    put("format", "yyyy-MM-dd HH:mm:ss");
                                } else if (DateUtil.isFormatted(value, "yyyy-MM-dd")) {
                                    put("format", "yyyy-MM-dd");
                                } else {
                                    throw new Exception(String.format("日期格式不正确: %s", value));
                                }
                            }
                        }});
                    }});
                }});
            }
        });

        return boolMap;
    }

    default String getErrorInfo(String errorInfo) {
        JsonNode jsonNode = JsonUtils.toJsonNode(errorInfo);
        Iterator<JsonNode> nodeIterable = jsonNode.get("error").get("root_cause").elements();
        while (nodeIterable.hasNext()) {
            return nodeIterable.next().get("reason").asText();
        }

        return errorInfo;
    }
}
