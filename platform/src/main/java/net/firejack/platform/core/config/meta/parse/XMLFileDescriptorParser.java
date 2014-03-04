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

package net.firejack.platform.core.config.meta.parse;

import net.firejack.platform.core.config.meta.IPackageDescriptor;
import net.firejack.platform.core.config.meta.construct.ConfigElementFactory;
import net.firejack.platform.core.config.meta.context.IUpgradeConfigContext;
import net.firejack.platform.core.config.meta.exception.ConfigSourceException;
import net.firejack.platform.core.config.meta.exception.ParseException;
import net.firejack.platform.core.config.meta.parse.xml.IPackageXmlProcessor;
import net.firejack.platform.core.utils.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;


class XMLFileDescriptorParser implements IPackageDescriptorParser<File> {

	private static final Logger logger = Logger.getLogger(XMLFileDescriptorParser.class);

	@Override
	public IPackageDescriptor parsePackageDescriptor(IUpgradeConfigContext<File> context) throws ParseException {
		File configFile;
		try {
			configFile = context.getConfigSource();
		} catch (ConfigSourceException e) {
			logger.error(e.getMessage(), e);
			throw new ParseException(e);
		}
		ConfigElementFactory elementFactory = ConfigElementFactory.getInstance();
		IPackageXmlProcessor xmlProcessor = elementFactory.getDefaultPackageXmlProcessor();

		try {
			String xml = FileUtils.readFileToString(configFile);
			return DescriptorParserUtils.convertDescriptor(xml, xmlProcessor);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ParseException(e.getMessage(), e);
		} catch (RuntimeException e) {
			logger.error(e.getMessage(), e);
			throw new ParseException(e.getMessage(), e);
		}
	}

}