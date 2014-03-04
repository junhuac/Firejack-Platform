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

package net.firejack.platform.model.upgrader;


import net.firejack.platform.core.utils.StringUtils;
import net.firejack.platform.model.upgrader.bean.IUpgradeModel;
import net.firejack.platform.model.upgrader.operator.AbstractOperator;
import net.firejack.platform.model.upgrader.operator.bean.DataSourceType;
import net.firejack.platform.model.upgrader.operator.bean.PackageType;
import net.firejack.platform.model.upgrader.serialization.IUpgradeModelSerializer;
import net.firejack.platform.model.upgrader.serialization.SerializerType;
import net.firejack.platform.model.upgrader.serialization.UpgradeModelSerializerFactory;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

/**
 * Define which operator need to use for execute upgrade commands.
 */
public class UpgradeExecutor {

    private static final Logger logger = Logger.getLogger(UpgradeExecutor.class);

    private Map<Class, AbstractOperator> operators;

    /**
     * @param operators
     */
    public void setOperators(Map<Class, AbstractOperator> operators) {
        this.operators = operators;
    }

    /**
     * This is a method which execute upgrade commands.
     *
     * @param version - upgrade version number with path to upgrade.xml
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public void upgrade(Version version, String packageLookup) throws JAXBException, FileNotFoundException {
        String upgradePathFile = version.getScriptPath();
        File upgradeFile = new File(upgradePathFile);
        upgrade(upgradeFile, null, packageLookup);
    }

    /**
     * @param upgradeFile
     * @param dataSource
     * @param domainLookup - package or domain lookup which associated with database for get data source
     * @return
     * @throws java.io.FileNotFoundException
     * @throws javax.xml.bind.JAXBException
     */
    public PackageType upgrade(File upgradeFile, DataSource dataSource, String domainLookup) throws JAXBException, FileNotFoundException {
        UpgradeModelSerializerFactory serializerFactory = UpgradeModelSerializerFactory.getInstance();
        IUpgradeModelSerializer modelSerializer = serializerFactory.produceSerializer(SerializerType.JAXB);
        PackageType packageType = modelSerializer.deSerializeFromReader(PackageType.class, new FileReader(upgradeFile));

        List<DataSourceType> dataSourceTypes = packageType.getDataSourceTypes();
        for (DataSourceType dataSourceType : dataSourceTypes) {
            String dataSourceLookup = dataSourceType.getPath() + "." + StringUtils.normalize(dataSourceType.getName());
            if (dataSourceLookup.equals(domainLookup)) {
                List<IUpgradeModel> types = dataSourceType.getCreateTableOrModifyColumnOrAddColumn();
                for (IUpgradeModel type : types) {
                    AbstractOperator operator = operators.get(type.getClass());
                    if (operator != null) {
                        if (dataSource != null) {
                            operator.setDataSource(dataSource);
                        }
                        operator.execute(type);
                    } else {
                        logger.warn("Command for type " + type.getClass().getSimpleName() + " has not implemented yet.");
                    }
                }
            } else {
                logger.info("Upgrade changes have not been found for data source with lookup: " + domainLookup);
            }
        }
        return packageType;
    }

    /**
     * This is a method which execute upgrade commands.
     *
     * @param xml - upgrade version number with path to upgrade.xml
     * @param domainLookup - package or domain lookup which associated with database for get data source
     * @throws JAXBException
     * @throws FileNotFoundException
     */
    public void upgrade(String xml, String domainLookup) throws JAXBException, FileNotFoundException {
        UpgradeModelSerializerFactory serializerFactory = UpgradeModelSerializerFactory.getInstance();
        IUpgradeModelSerializer modelSerializer = serializerFactory.produceSerializer(SerializerType.JAXB);
        PackageType packageType = modelSerializer.deSerialize(PackageType.class, xml);

        List<DataSourceType> dataSourceTypes = packageType.getDataSourceTypes();
        for (DataSourceType dataSourceType : dataSourceTypes) {
            String dataSourceLookup = StringUtils.normalize(dataSourceType.getPath() + "." + dataSourceType.getName());
            if (dataSourceLookup.equals(domainLookup)) {
                List<IUpgradeModel> types = dataSourceType.getCreateTableOrModifyColumnOrAddColumn();
                for (IUpgradeModel type : types) {
                    AbstractOperator operator = operators.get(type.getClass());
                    if (operator != null) {
                        operator.execute(type);
                    } else {
                        logger.warn("Command for type " + type.getClass().getSimpleName() + " has not implemented yet.");
                    }
                }
            }
        }
    }
}
