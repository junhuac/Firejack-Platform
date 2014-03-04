/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package net.firejack.platform.model.service.reverse.analyzer;

import net.firejack.platform.api.registry.model.FieldType;
import net.firejack.platform.model.service.reverse.bean.Column;
import net.firejack.platform.model.service.reverse.bean.Table;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OracleTableAnalyzer extends AbstractTableAnalyzer {

    public OracleTableAnalyzer(DataSource dataSource, String sid, String schema) {
        super(dataSource, sid, schema);
    }

    @Override
    public Column createColumn(ResultSet rs, Table table) throws SQLException {
        Column column = new Column();
        column.setName(rs.getString("COLUMN_NAME"));
        column.setDataType(rs.getString("TYPE_NAME"));
        column.setLength(rs.getInt("COLUMN_SIZE"));
        column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
        column.setNullable(rs.getBoolean("NULLABLE"));
        column.setComment(rs.getString("REMARKS"));
        FieldType fieldType = detectFieldType(column);
        column.setFieldType(fieldType);
        String defaultValue = rs.getString("COLUMN_DEF");
        if (defaultValue != null) {
            defaultValue = defaultValue.trim();
            defaultValue = defaultValue.replaceAll("^\\s*'(.*?)'\\s*$", "$1");
        }
        column.setDefaultValue(defaultValue);
        column.setTable(table);
        return column;
    }

    private FieldType detectFieldType(Column column) {
        String dataType = column.getDataType();
        Integer length = column.getLength();
        Integer decimalDigits = column.getDecimalDigits();
        String defaultValue = column.getDefaultValue();
        FieldType fieldType = FieldType.OBJECT;
        if (dataType.matches("(?i)NUMBER")) {
            if (decimalDigits <= 0) {
                if (length == 1) {
                    fieldType = FieldType.FLAG;
                } else if (length <= 11){
	                fieldType = FieldType.INTEGER_NUMBER;
                } else {
                    fieldType = FieldType.LARGE_NUMBER;
                }
            } else {
                fieldType = FieldType.DECIMAL_NUMBER;
            }
        } else if (dataType.matches("(?i)(FLOAT|BINARY_FLOAT|BINARY_DOUBLE)")) {
            fieldType = FieldType.DECIMAL_NUMBER;
        } else if (dataType.matches("(?i)ROWID")) {
            fieldType = FieldType.TINY_TEXT;
        } else if (dataType.matches("(?i)(VARCHAR2|NVARCHAR2|CHAR|NCHAR|UROWID)")) {
            if (length <= 16) {
                fieldType = FieldType.CODE;
            } else if (length <= 64) {
                fieldType = FieldType.TINY_TEXT;
            } else if (length <= 255) {
                fieldType = FieldType.SHORT_TEXT;
            } else if (length <= 1024) {
                fieldType = FieldType.MEDIUM_TEXT;
            } else if (length <= 2048) {
                fieldType = FieldType.URL;
            } else if (length <= 4096) {
                fieldType = FieldType.DESCRIPTION;
            } else {
                fieldType = FieldType.LONG_TEXT;
            }
        } else if (dataType.matches("(?i)(N?[BC]LOB|XMLType)")) {
            fieldType = FieldType.UNLIMITED_TEXT;
        } else if (dataType.matches("(?i)(INTERVAL\\sYEAR.*|DATE)")) {
            fieldType = FieldType.DATE;
        } else if (dataType.matches("(?i)INTERVAL\\sDAY.*")) {
            fieldType = FieldType.TIME;
        } else if (dataType.matches("(?i)TIMESTAMP.*")) {
            if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue)) {
                fieldType = FieldType.CREATION_TIME;
            } else {
                fieldType = FieldType.UPDATE_TIME;
            }
        } else if (dataType.matches("(?i)(BOOLEAN)")) {
            fieldType = FieldType.FLAG;
        }
        return fieldType;
    }

}
