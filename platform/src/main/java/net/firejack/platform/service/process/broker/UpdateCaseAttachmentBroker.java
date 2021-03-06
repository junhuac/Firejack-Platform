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

import net.firejack.platform.api.process.domain.CaseAttachment;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.domain.NamedValues;
import net.firejack.platform.core.model.registry.process.CaseAttachmentModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.process.ICaseAttachmentStore;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Class encapsulates the functionality of case attachment modification
 */
@TrackDetails
@Component("updateCaseAttachmentBroker")
public class UpdateCaseAttachmentBroker extends ServiceBroker<ServiceRequest<NamedValues>, ServiceResponse<CaseAttachment>> {

    @Autowired
    @Qualifier("caseAttachmentStore")
    private ICaseAttachmentStore store;

    /**
     * Invokes data access layer in order to update the case attachment
     *
     * @param request service request containing the attachment
     * @return service response containing the attachment and the information about the modification success
     * @throws net.firejack.platform.core.exception.BusinessFunctionException
     */
    @Override
    protected ServiceResponse<CaseAttachment> perform(ServiceRequest<NamedValues> request) throws Exception {
        CaseAttachment caseAttachment = (CaseAttachment)request.getData().get("caseAttachment");
        CaseAttachmentModel caseAttachmentModel = factory.convertFrom(CaseAttachmentModel.class, caseAttachment);
        CaseAttachmentModel foundCaseAttachment = store.findById(caseAttachment.getId());
        caseAttachmentModel.setCreated(foundCaseAttachment.getCreated());
        caseAttachmentModel.setFilename(foundCaseAttachment.getFilename());
        caseAttachmentModel.setProcessCase(foundCaseAttachment.getProcessCase());
        store.saveOrUpdate(caseAttachmentModel);
        caseAttachment = factory.convertTo(CaseAttachment.class, caseAttachmentModel);
        return new ServiceResponse<CaseAttachment>(caseAttachment, "Case attachment updated successfully.", true);
    }
    
}
