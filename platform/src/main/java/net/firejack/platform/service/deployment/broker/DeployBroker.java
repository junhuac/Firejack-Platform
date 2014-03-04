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

package net.firejack.platform.service.deployment.broker;


import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.domain.NamedValues;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.utils.FileUtils;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;

@TrackDetails
@Component
public class DeployBroker extends ServiceBroker<ServiceRequest<NamedValues>, ServiceResponse> {

	@Value("${debug.mode}")
	private boolean debug;
	private File webapps = new File(System.getenv("CATALINA_BASE"), "webapps");

	@Override
	protected ServiceResponse perform(ServiceRequest<NamedValues> request) throws Exception {
		Long packageId = (Long) request.getData().get("packageId");
		String name = (String) request.getData().get("name");
		String file = (String) request.getData().get("file");
		String host = InetAddress.getLocalHost().getHostName();

		if (debug) {
			webapps = new File(System.getenv("CATALINA_HOME"), "webapps");
		}

        InputStream stream = OPFEngine.RegistryService.getPackageArchive(packageId, file);

        File app = new File(webapps, name + ".temp");
        FileOutputStream outputStream = FileUtils.openOutputStream(app);
		if (stream != null) {
			IOUtils.copy(stream, outputStream);
			IOUtils.closeQuietly(outputStream);
			IOUtils.closeQuietly(stream);

			File war = new File(app.getParent(), name);
			FileUtils.deleteQuietly(war);
			FileUtils.moveFile(app, war);
		}

		return new ServiceResponse("Deploy to server " + host + " successfully", true);
	}
}
