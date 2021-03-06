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
import net.firejack.platform.core.broker.DeleteBroker;
import net.firejack.platform.core.domain.SimpleIdentifier;
import net.firejack.platform.core.model.registry.process.CaseAttachmentModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.IStore;
import net.firejack.platform.core.store.process.ICaseAttachmentStore;
import net.firejack.platform.core.utils.OpenFlame;
import net.firejack.platform.model.helper.FileHelper;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Class encapsulates the functionality of case attachment deletion
 */
@TrackDetails
@Component("deleteCaseAttachmentBroker")
public class DeleteCaseAttachmentBroker extends DeleteBroker<CaseAttachmentModel> {

    @Autowired
	private FileHelper helper;

    @Autowired
    @Qualifier("caseAttachmentStore")
    protected ICaseAttachmentStore store;

    @Override
    protected IStore<CaseAttachmentModel, Long> getStore() {
        return store;
    }

    /**
     * Invokes data access layer in order to remove the case attachment
     *
     * @param request service request containing ID of the case attachment to be removed
     * @return service response containing information about the success of the removal operation
     * @throws net.firejack.platform.core.exception.BusinessFunctionException
     */
    @Override
    protected ServiceResponse perform(ServiceRequest<SimpleIdentifier<Long>> request) throws Exception {
        ServiceResponse response;
        Long id = request.getData().getIdentifier();
        CaseAttachmentModel caseAttachment = store.findById(id);
        if (caseAttachment != null) {
            Long processCaseId = caseAttachment.getProcessCase().getId();

	        store.deleteById(id);
	        OPFEngine.FileStoreService.deleteFile(OpenFlame.FILESTORE_CONTENT, id.toString(), helper.getCase(), processCaseId.toString(), "attachments");
	        response = new ServiceResponse("CaseAttachment has deleted successfully.", true);
        } else {
            response = new ServiceResponse("CaseAttachment has not been found with id:[" + id + "]", false);
        }
        return response;
    }
    
}
