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

package net.firejack.platform.service.registry.broker.registry;

import com.sun.jersey.core.util.Base64;
import net.firejack.platform.api.registry.domain.ServerNodeConfig;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.exception.BusinessFunctionException;
import net.firejack.platform.core.model.registry.domain.PackageModel;
import net.firejack.platform.core.model.registry.domain.SystemModel;
import net.firejack.platform.core.model.registry.system.ServerModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.registry.IPackageStore;
import net.firejack.platform.core.store.registry.IServerStore;
import net.firejack.platform.core.store.registry.ISystemStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.security.x509.X509CertImpl;

import java.security.PublicKey;
import java.security.cert.CertificateException;


@Component
public class RegisterServerBroker extends ServiceBroker<ServiceRequest<ServerNodeConfig>, ServiceResponse> {

    @Autowired
    private IServerStore serverStore;
    @Autowired
    private ISystemStore systemStore;
    @Autowired
    private IPackageStore packageStore;

    @Override
    protected ServiceResponse perform(ServiceRequest<ServerNodeConfig> request)
            throws BusinessFunctionException {
        ServerNodeConfig data = request.getData();

        ServiceResponse response;
        try {
	        PackageModel model = packageStore.findByLookup(data.getLookup());
	        if (model.getSystem() == null) {
		        throw new IllegalStateException("Couldn't find an associated system by the specified package: " + data.getLookup());
	        }

	        SystemModel system = systemStore.findById(model.getSystem().getId());
	        if (system != null) {
	            ServerModel server = new ServerModel();
	            server.setName(data.getServerName());
	            server.setPath(system.getLookup());
	            server.setParent(system);
	            server.setServerName(data.getHost());
	            server.setPort(data.getPort());
	            server.setActive(false);
	            X509CertImpl cert = new X509CertImpl(data.getCert());
	            PublicKey publicKey = cert.getPublicKey();
	            byte[] encodedPubKey = Base64.encode(publicKey.getEncoded());
	            server.setPublicKey(new String(encodedPubKey));

	            try {
	                serverStore.save(server);
	                response = new ServiceResponse("Server Node registered.", true);
	            } catch (Exception e) {
	                logger.error(e.getMessage(), e);
	                response = new ServiceResponse(e.getMessage(), false);
	            }
	        } else {
	            response = new ServiceResponse("Failed to find parent system.", false);
	        }
        } catch (CertificateException e) {
            logger.error(e.getMessage(), e);
            response = new ServiceResponse(e.getMessage(), false);
        }
        return response;
    }

}