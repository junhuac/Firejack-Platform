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

package net.firejack.platform.service.directory.broker.application;

import net.firejack.platform.api.directory.domain.Application;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.domain.SimpleIdentifier;
import net.firejack.platform.core.model.registry.directory.ApplicationModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.registry.IApplicationStore;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@TrackDetails
@Component
public class ReadApplicationBroker extends ServiceBroker
        <ServiceRequest<SimpleIdentifier<Long>>, ServiceResponse<Application>> {

    @Autowired
    @Qualifier("applicationStore")
    private IApplicationStore store;

    @Override
    protected ServiceResponse<Application> perform(ServiceRequest<SimpleIdentifier<Long>> request) throws Exception {
        Long applicationId = request.getData().getIdentifier();

        ServiceResponse<Application> response;
        if (applicationId == null) {
            response = new ServiceResponse<Application>("Application Id is null", false);
        } else {
            ApplicationModel model = store.findById(applicationId);
            if (model == null) {
                response = new ServiceResponse<Application>("Failed to find application by specified id.", false);
            } else {
                Application application = factory.convertTo(Application.class, model);
                response = new ServiceResponse<Application>(application, "Application found for specified id.", true);
            }
        }
        return response;
    }

}