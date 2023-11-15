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

package com.dtstack.flinkx.vertica;

import com.dtstack.flinkx.enums.EDatabaseType;
import com.dtstack.flinkx.rdb.BaseDatabaseMeta;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The class of Oracle database prototype
 *
 * Company: www.dtstack.com
 * @author huyifan.zju@163.com
 */
public class VerticaDatabaseMeta extends BaseDatabaseMeta {

    @Override
    public String quoteTable(String table) {
        table = table.replace("\"","");
        String[] part = table.split("\\.");
        if(part.length == DB_TABLE_PART_SIZE) {
            table = getStartQuote() + part[0] + getEndQuote() + "." + getStartQuote() + part[1] + getEndQuote();
        } else {
            table = getStartQuote() + table + getEndQuote();
        }
        return table;
    }

    @Override
    public EDatabaseType getDatabaseType() {
        return EDatabaseType.Vertica;
    }

    @Override
    public String getDriverClass() {
        return "com.vertica.jdbc.Driver";
    }

    @Override
    public String getSqlQueryFields(String tableName) {
        return "SELECT /*+FIRST_ROWS*/ * FROM " + tableName + " LIMIT 0";
    }

    @Override
    public String getSqlQueryColumnFields(List<String> column, String table) {
        return "SELECT /*+FIRST_ROWS*/ " + quoteColumns(column) + " FROM " + quoteTable(table) + " LIMIT 0";
    }

    @Override
    public String quoteValue(String value, String column) {
        return String.format("%s as %s%s%s",value, getStartQuote(), column, getEndQuote());
    }

    @Override
    public String getSplitFilter(String columnName) {
        return String.format("mod(%s, ${N}) = ${M}", getStartQuote() + columnName + getEndQuote());
    }

    @Override
    public String getSplitFilterWithTmpTable(String tmpTable, String columnName) {
        return String.format("mod(%s.%s, ${N}) = ${M}", tmpTable, getStartQuote() + columnName + getEndQuote());
    }

    private String makeValues(int nCols) {
        return "(" + StringUtils.repeat("?", ",", nCols) + ")";
    }

    @Override
    protected String makeValues(List<String> column) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String makeReplaceValues(List<String> column, List<String> fullColumn){
        List<String> values = new ArrayList<>();
        boolean contains = false;

        for (String col : column) {
            values.add("? " + quoteColumn(col));
        }

        for (String col : fullColumn) {
            for (String c : column) {
                if (c.equalsIgnoreCase(col)){
                    contains = true;
                    break;
                }
            }

            if (contains){
                contains = false;
                continue;
            } else {
                values.add("null "  + quoteColumn(col));
            }

            contains = false;
        }

        return "SELECT " + org.apache.commons.lang3.StringUtils.join(values,",") + " FROM DUAL";
    }

    @Override
    public String getRowNumColumn(String orderBy) {
        throw new RuntimeException("Not support row_number function");
    }

    @Override
    public int getFetchSize(){
        return 1000;
    }

    @Override
    public int getQueryTimeout(){
        return 3000;
    }
}
