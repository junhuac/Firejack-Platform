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

package net.firejack.platform.service.process.broker;

import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.api.content.domain.FileInfo;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.domain.SimpleIdentifier;
import net.firejack.platform.core.model.registry.process.CaseAttachmentModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.process.ICaseAttachmentStore;
import net.firejack.platform.core.utils.OpenFlame;
import net.firejack.platform.model.helper.FileHelper;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/** Class encapsulates the functionality of downloading a case attachment */
@TrackDetails
@Component("downloadCaseAttachmentBroker")
public class DownloadCaseAttachmentBroker extends ServiceBroker<ServiceRequest<SimpleIdentifier<Long>>, ServiceResponse<FileInfo>> {

	@Autowired
	private FileHelper helper;

	@Autowired
	@Qualifier("caseAttachmentStore")
	protected ICaseAttachmentStore store;

	/**
	 * Invokes data access layer to find the case attachment by its' ID and prepares the file for download
	 *
	 * @param request service request containing the ID of the attachment
	 *
	 * @return service response containing the information about the success of the operation and FileInfo value object if possible
	 *
	 * @throws net.firejack.platform.core.exception.BusinessFunctionException
	 */
	@Override
	protected ServiceResponse<FileInfo> perform(ServiceRequest<SimpleIdentifier<Long>> request) throws Exception {
		Long id = request.getData().getIdentifier();
		CaseAttachmentModel caseAttachment = store.findById(id);
		if (caseAttachment != null) {
			Long processCaseId = caseAttachment.getProcessCase().getId();

			InputStream stream = OPFEngine.FileStoreService.download(OpenFlame.FILESTORE_CONTENT, id.toString(),helper.getCase(), processCaseId.toString(), "attachments");
			FileInfo file = new FileInfo();
			file.setFilename(caseAttachment.getFilename());
			file.setStream(stream);
			return new ServiceResponse<FileInfo>(file, "File downloaded.", true);
		}
		return new ServiceResponse<FileInfo>("No case attachment for download.", false);
	}

}
