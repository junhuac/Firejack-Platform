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

import net.firejack.platform.api.registry.domain.RegistryNodeTree;
import net.firejack.platform.core.broker.ReadAllBroker;
import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.process.ProcessModel;
import net.firejack.platform.core.store.lookup.ILookupStore;
import net.firejack.platform.core.store.registry.IProcessStore;
import net.firejack.platform.core.utils.TreeNodeFactory;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@TrackDetails
@Component("readAllProcessTreeBroker")
public class ReadAllProcessTreeBroker extends ReadAllBroker<ProcessModel, RegistryNodeTree> {

	@Autowired
	private IProcessStore store;
    @Autowired
    private TreeNodeFactory treeNodeFactory;

	@Override
	protected ILookupStore<ProcessModel, Long> getStore() {
		return store;
	}

    protected List<RegistryNodeTree> convertToDTOs(List<ProcessModel> processModels) {
        List<RegistryNodeTree> nodeList = new ArrayList<RegistryNodeTree>();
        for (RegistryNodeModel model : processModels) {
            RegistryNodeTree node = treeNodeFactory.convertTo(model);
            node.setLeaf(true);
            nodeList.add(node);
        }
        return nodeList;
    }
}
