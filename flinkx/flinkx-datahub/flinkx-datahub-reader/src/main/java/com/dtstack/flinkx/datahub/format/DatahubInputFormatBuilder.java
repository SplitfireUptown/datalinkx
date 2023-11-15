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
package com.dtstack.flinkx.datahub.format;

import com.dtstack.flinkx.inputformat.BaseRichInputFormatBuilder;
import com.dtstack.flinkx.reader.MetaColumn;
import org.apache.commons.lang3.StringUtils;

import java.util.List;


/**
 * Date: 2020/12/07
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class DatahubInputFormatBuilder extends BaseRichInputFormatBuilder {

    private DatahubInputFormat format;

    public DatahubInputFormatBuilder(DatahubInputFormat format) {
        super.format = this.format = format;
    }

    public void setEndpoint(String endpoint) {
        format.endpoint = endpoint;
    }

    public void setAccessId(String accessId) {
        format.accessId = accessId;
    }

    public void setAccessKey(String accessKey) {
        format.accessKey = accessKey;
    }

    public void setProject(String project) {
        format.project = project;
    }

    public void setTopic(String topic) {
        format.topic = topic;
    }

    public void setSubscriptionId(String subscriptionId) {
        format.subscriptionId = subscriptionId;
    }

    public void setFetchSize(Integer fetchSize) {
        format.fetchSize = fetchSize;
    }

    public void setMetaColumns(List<MetaColumn> metaColumns) {
        format.metaColumns = metaColumns;
    }

    @Override
    protected void checkFormat() {
        StringBuilder sb = new StringBuilder(128);
        if (StringUtils.isBlank(format.topic)) {
            sb.append("No datahub topic supplied;\n");
        }

        if (sb.length() > 0) {
            throw new IllegalArgumentException(sb.toString());
        }
    }
}
