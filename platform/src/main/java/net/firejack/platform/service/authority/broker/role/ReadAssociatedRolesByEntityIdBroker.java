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

package net.firejack.platform.service.authority.broker.role;

import net.firejack.platform.api.authority.domain.Role;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.domain.SimpleIdentifier;
import net.firejack.platform.core.model.registry.authority.RoleModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.registry.IRoleStore;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@TrackDetails
public class ReadAssociatedRolesByEntityIdBroker extends ServiceBroker
        <ServiceRequest<SimpleIdentifier<Long>>, ServiceResponse<Role>> {

    @Autowired
    private IRoleStore roleStore;

    @Override
    protected ServiceResponse<Role> perform(
            ServiceRequest<SimpleIdentifier<Long>> request) throws Exception {
        Long entityId = request.getData().getIdentifier();
        ServiceResponse<Role> response;
        if (entityId == null) {
            response = new ServiceResponse<Role>("EntityId should not be null.", false);
        } else {
            List<RoleModel> roles = roleStore.findEntityAssociatedContextRoles(entityId);
            List<Role> contextRoles = factory.convertTo(Role.class, roles);
            String message = contextRoles == null || contextRoles.isEmpty() ?
                    "No allowable Context Roles were specified for the entity." :
                    "Context Roles associated with specified entity were found.";
            response = new ServiceResponse<Role>(contextRoles, message, true);
        }
        return response;
    }

}