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

package net.firejack.platform.service.registry.broker.wizard;

import net.firejack.platform.api.registry.domain.Wizard;
import net.firejack.platform.core.broker.ReadBroker;
import net.firejack.platform.core.model.registry.wizard.WizardModel;
import net.firejack.platform.core.store.IStore;
import net.firejack.platform.core.store.registry.IWizardStore;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@TrackDetails
@Component("readWizardBroker")
public class ReadWizardBroker extends ReadBroker<WizardModel, Wizard> {

	@Autowired
	private IWizardStore store;

	@Override
	protected IStore<WizardModel, Long> getStore() {
		return store;
	}
}
