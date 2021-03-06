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

package net.firejack.platform.service.directory.broker.field;

import net.firejack.platform.api.directory.domain.UserProfileField;
import net.firejack.platform.api.directory.domain.UserProfileFieldTree;
import net.firejack.platform.core.broker.OPFSaveBroker;
import net.firejack.platform.core.model.user.UserProfileFieldGroupModel;
import net.firejack.platform.core.model.user.UserProfileFieldModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.store.user.IUserProfileFieldGroupStore;
import net.firejack.platform.core.store.user.IUserProfileFieldStore;
import net.firejack.platform.core.validation.ValidationMessage;
import net.firejack.platform.core.validation.exception.RuleValidationException;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@TrackDetails
@Component("createUserProfileFieldBroker")
public class CreateUserProfileFieldBroker extends OPFSaveBroker<UserProfileFieldModel, UserProfileField, UserProfileFieldTree> {

	@Autowired
    @Qualifier("userProfileFieldStore")
	private IUserProfileFieldStore store;
	@Autowired
    @Qualifier("userProfileFieldGroupStore")
	protected IUserProfileFieldGroupStore groupStore;

	@Override
	protected String getSuccessMessage(boolean isNew) {
		return "User profile field has created successfully";
	}

	@Override
	protected List<ValidationMessage> specificArgumentsValidation(ServiceRequest<UserProfileField> request) throws RuleValidationException {
		List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
		UserProfileField userProfileField = request.getData();
		UserProfileFieldModel sameUserProfile = store.findUserProfileFieldByRegistryNodeIdAndName(userProfileField.getParentId(), userProfileField.getName());
		if (sameUserProfile != null) {
			validationMessages.add(new ValidationMessage("name", "user_profile_field.already.exist.with.name"));
		}
		if (userProfileField.getUserProfileFieldGroupId() != null) {
			UserProfileFieldGroupModel profileFieldGroup = groupStore.findUserProfileFieldGroupById(userProfileField.getUserProfileFieldGroupId(), false);
			if (profileFieldGroup == null) {
				ValidationMessage validationMessage = new ValidationMessage(null, "user_profile_field_group.not.exist");
				validationMessages.add(validationMessage);
			}
		}
		return validationMessages;
	}

	@Override
	protected UserProfileFieldModel convertToEntity(UserProfileField model) {
		return factory.convertFrom(UserProfileFieldModel.class, model);
	}

	@Override
	protected UserProfileFieldTree convertToModel(UserProfileFieldModel entity) {
		return factory.convertTo(UserProfileFieldTree.class, entity);
	}

	@Override
	protected void save(UserProfileFieldModel model) throws Exception {
		model.setChildCount(0);
		store.saveOrUpdate(model);
	}
}
