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
import net.firejack.platform.service.directory.broker.ReadBaseUserBroker;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@TrackDetails
@Component("readUserBroker")
public class ReadUserBroker extends ReadBaseUserBroker<UserModel, User> {

    @Autowired
    @Qualifier("userStore")
    protected IUserStore store;

	@Override
	protected IUserStore getStore() {
		return store;
	}

    protected UserModel getUserById(Long id) {
        UserModel user = getStore().findById(id);
        user.setPassword(null);
        return user;
    }

}
