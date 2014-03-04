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

package net.firejack.platform.core.config.translate.sql;

import net.firejack.platform.api.registry.model.FieldType;
import net.firejack.platform.core.config.meta.IEntityElement;
import net.firejack.platform.core.config.meta.IFieldElement;
import net.firejack.platform.core.config.meta.IIndexElement;
import net.firejack.platform.core.config.meta.expr.IExpression;
import net.firejack.platform.core.config.meta.expr.IExpressionSupport;
import net.firejack.platform.core.config.translate.SqlTranslationResult;
import net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException;
import net.firejack.platform.core.config.translate.sql.token.ForeignKeyProcessor;
import net.firejack.platform.model.upgrader.dbengine.DialectType;

import java.util.Set;


public interface ISqlDialect extends IExpressionSupport<String>, ISqlSupport {

    public static final String TOKEN_SPACE = " ";
    public static final String TOKEN_DOUBLE_SPACE = "  ";
    public static final String TOKEN_COMMA_SPACE = ", ";
    public static final String TOKEN_COMMA_EOL = ",\n";
    public static final String TOKEN_BRACE_EOL = "(\n";

    public static final String TOKEN_NULL = "NULL";
    public static final String TOKEN_IS_NULL = " IS NULL";
    public static final String TOKEN_IS_NOT_NULL = " IS NOT NULL";
    public static final String TOKEN_NOT_NULL_SPACE = "NOT NULL ";
    public static final String TOKEN_NULL_SPACE = "NULL ";
    public static final String TOKEN_DEFAULT_SPACE = "DEFAULT ";

    public static final String TOKEN_UNIQUE1 = "  UNIQUE `";
    public static final String TOKEN_UNIQUE2 = "` ";

    public static final String MSG_CANT_PROCESS_VALUE_OF_TYPE1 = "Can't process value of type [";
    public static final String MSG_CANT_PROCESS_VALUE_OF_TYPE2 = "]";
    public static final String MSG_WRONG_REFERENCE_FIELD_NAME = "Wrong reference field name specified.";
    public static final String MSG_COULD_NOT_PROCESS_NULL_FUNCTION = "Could not process function which is null.";
    public static final String MSG_FIELD_TYPE_PARAMETER_IS_NULL = "Field type parameter should not be null";
    public static final String MSG_FAILED_TO_CALCULATE_TABLE_NAME = "Failed to calculate table name.";
    public static final String MSG_FAILED_TO_EVAL_EXPR = "Failed to evaluate expression.";
    public static final String MSG_NULL_NOT_ALLOWED = "Null value does not allowed.";

    /**
     * @return
     */
    DialectType getType();

    void initializeScript(SqlTranslationResult resultState);

    /**
     * @param entity
     * @return
     */
    String getTableName(IEntityElement entity);

    /**
     * @param entity
     * @param createIdField
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String createTable(IEntityElement entity, boolean createIdField) throws SqlProcessingException;

    /**
     * @param entity
     * @param refField1
     * @param refField2
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String createStagingTable(IEntityElement entity, String refField1, String refField2) throws SqlProcessingException;

    /**
     * @param oldEntity
     * @param newEntity
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String modifyTableName(IEntityElement oldEntity, IEntityElement newEntity) throws SqlProcessingException;

    /**
     * @param oldTableName
     * @param newTableName
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String modifyTableName(String oldTableName, String newTableName) throws SqlProcessingException;

    /**
     * @param entityName
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String dropTable(String entityName) throws SqlProcessingException;

    /**
     * @param entity
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String dropTable(IEntityElement entity) throws SqlProcessingException;

    /**
     * @param entity
     * @param field
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String addColumn(IEntityElement entity, IFieldElement field) throws SqlProcessingException;

    /**
     * @param entity
     * @param oldColumnDefinition
     * @param newColumnDefinition
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String changeColumn(IEntityElement entity, IFieldElement oldColumnDefinition, IFieldElement newColumnDefinition) throws SqlProcessingException;

    /**
     * @param entityName
     * @param columnName
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String dropColumn(String entityName, String columnName) throws SqlProcessingException;

    /**
     * @param entity
     * @param columnDefinition
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String dropColumn(IEntityElement entity, IFieldElement columnDefinition) throws SqlProcessingException;

    /**
     * @param keyName
     * @param fkTable
     * @param fkFieldName
     * @param pkTable
     * @return
     */
    ForeignKeyProcessor getAddFkToken(String keyName, String fkTable, String fkFieldName, String pkTable, String pkFieldName);

    /**
     * @param keyName
     * @param tableWithUniqueKeys
     * @param ukParticipant
     * @param ukParticipants
     * @return
     */
    String addUniqueKey(String keyName, String tableWithUniqueKeys, String ukParticipant, String... ukParticipants);

    /**
     * @param keyName
     * @param tableWithUniqueKeys
     * @param ukParticipants
     * @return
     */
    String addUniqueKey(String keyName, String tableWithUniqueKeys, Set<String> ukParticipants);

    /**
     * @param tableName
     * @param keyName
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String dropForeignKey(String tableName, String keyName) throws SqlProcessingException;

    String addIndex(IEntityElement rootEntity, IIndexElement diffTarget);

    /**
     * @param tableName
     * @param keyName
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String dropIndex(String tableName, String keyName) throws SqlProcessingException;

    /**
     * @param fieldType
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String getColumnType(FieldType fieldType) throws SqlProcessingException;

    /**
     * @param entity
     * @param columnNames
     * @param selectStatement
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String insertWithSelect(IEntityElement entity, String[] columnNames, String selectStatement) throws SqlProcessingException;

    /**
     * @param entity
     * @param whereCondition
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String delete(IEntityElement entity, IExpression<String> whereCondition) throws SqlProcessingException;

    /**
     * @param condition
     * @return
     * @throws net.firejack.platform.core.config.translate.sql.exception.SqlProcessingException
     *
     */
    String processExpression(IExpression<String> condition) throws SqlProcessingException;

}