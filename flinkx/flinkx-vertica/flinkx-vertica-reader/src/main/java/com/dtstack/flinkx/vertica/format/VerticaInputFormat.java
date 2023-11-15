/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.flinkx.vertica.format;

import com.dtstack.flinkx.rdb.inputformat.JdbcInputFormat;
import org.apache.flink.types.Row;

import java.io.IOException;
import java.sql.Blob;
import java.text.SimpleDateFormat;

import static com.dtstack.flinkx.rdb.util.DbUtil.clobToString;

/**
 * Date: 2019/09/19
 * Company: www.dtstack.com
 *
 * @author tudou
 */
public class VerticaInputFormat extends JdbcInputFormat {

    @Override
    public Row nextRecordInternal(Row row) throws IOException {
        if (!hasNext) {
            return null;
        }
        row = new Row(columnCount);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int pos = 0; pos < row.getArity(); pos++) {
                Object obj = resultSet.getObject(pos + 1);
                if(obj != null) {
                    if (metaColumns.get(pos).getValue() != null && "now()".equals(metaColumns.get(pos).getValue())) {
                        obj = dateFormat.format(new java.util.Date());
                    } else if (metaColumns.get(pos).getTimeFormat() != null) {
                        try {
                            obj = dateFormat.format(metaColumns.get(pos).getTimeFormat().parse(obj.toString()));
                        } catch (Exception e) {
                            LOG.info("fmt error");
                        }
                    } else if ((obj instanceof java.util.Date
                            || obj.getClass().getSimpleName().toUpperCase().contains("TIMESTAMP")) ) {
                        obj = resultSet.getTimestamp(pos + 1);
                    } else if (obj instanceof Blob) {
                        Blob blob = (Blob) obj;
                        long length = blob.length();
                        LOG.info("读取到blob字段数据，数据长度为：" + length);
                        obj = blob.getBytes(1, Integer.parseInt(length + ""));
                    } else if (obj instanceof Boolean) {
                        obj = ((Boolean)obj) ? 1 : 0;
                    }
                    obj = clobToString(obj);
                }

                row.setField(pos, obj);
            }
            return super.nextRecordInternal(row);
        }catch (Exception e) {
            throw new IOException("Couldn't read data - " + e.getMessage(), e);
        }
    }
}
