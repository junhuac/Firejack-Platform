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

package net.firejack.platform.service.site.broker.navigation;

import net.firejack.platform.api.site.domain.NavigationElement;
import net.firejack.platform.core.broker.ReadBroker;
import net.firejack.platform.core.model.registry.site.NavigationElementModel;
import net.firejack.platform.core.store.registry.IRegistryNodeStore;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component("readNavigationElementBroker")
@TrackDetails
public class ReadNavigationElementBroker
        extends ReadBroker<NavigationElementModel, NavigationElement> {

    @Autowired
    @Qualifier("navigationElementStore")
    private IRegistryNodeStore<NavigationElementModel> store;


    @Override
    protected IRegistryNodeStore<NavigationElementModel> getStore() {
        return store;
    }

}