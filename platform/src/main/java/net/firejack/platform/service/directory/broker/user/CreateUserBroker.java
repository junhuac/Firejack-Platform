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

package net.firejack.platform.service.directory.broker.user;

import net.firejack.platform.api.directory.domain.User;
import net.firejack.platform.core.model.user.UserModel;
import net.firejack.platform.core.store.user.IUserStore;
import net.firejack.platform.core.utils.SecurityHelper;
import net.firejack.platform.service.directory.broker.CreateBaseUserBroker;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@TrackDetails
@Component("createUserBroker")
public class CreateUserBroker extends CreateBaseUserBroker<UserModel, User> {

	@Autowired
    @Qualifier("userStore")
	private IUserStore store;

    @Override
    protected IUserStore getStore() {
        return store;
    }

    @Override
	protected String getSuccessMessage(boolean isNew) {
		return "User has created successfully";
	}

	@Override
	protected UserModel convertToEntity(User model) {
		return factory.convertFrom(UserModel.class, model);
	}

	@Override
	protected User convertToModel(UserModel entity) {
		return factory.convertTo(User.class, entity);
	}

    @Override
	protected void save(UserModel model) throws Exception {
		String hashPassword = SecurityHelper.hash(model.getPassword());
		model.setPassword(hashPassword);
		getStore().save(model);
	}

}
