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

package net.firejack.platform.service.filestore.broker.file;

import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.domain.NamedValues;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.registry.IFileStore;
import net.firejack.platform.core.utils.ArrayUtils;
import net.firejack.platform.core.utils.OpenFlameSpringContext;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.stereotype.Component;

/**
 * Class encapsulates the file deletion functionality
 */
@Component
@TrackDetails
public class DeleteFileBroker extends ServiceBroker<ServiceRequest<NamedValues>, ServiceResponse> {

	/**
	 * Removes the file with the specified path and name
	 *
	 * @param request - the message passed to the business function with all data required
	 * @return information about the success of the deletion operation
	 * @throws net.firejack.platform.core.exception.BusinessFunctionException
	 *
	 */
	@Override
	protected ServiceResponse perform(ServiceRequest<NamedValues> request) throws Exception {
		String lookup = (String) request.getData().get("lookup");
		String filename = (String) request.getData().get("filename");
		String[] path = (String[]) request.getData().get("path");

		IFileStore fileStore = OpenFlameSpringContext.getBean(lookup);
		path = (String[]) ArrayUtils.add(path, filename);
		fileStore.delete(path);

		return new ServiceResponse("File deleted successfully.", true);
	}
}