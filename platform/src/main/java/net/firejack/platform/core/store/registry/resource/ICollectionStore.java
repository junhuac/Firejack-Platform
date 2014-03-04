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

package net.firejack.platform.core.store.registry.resource;

import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.resource.CollectionModel;
import net.firejack.platform.core.store.registry.IRegistryNodeStore;

import java.util.List;

public interface ICollectionStore extends IRegistryNodeStore<CollectionModel> {

    CollectionModel findOrCreateCollection(RegistryNodeModel registryNode);

    List<RegistryNodeModel> findReferences(Long collectionId);

    void associateCollectionWithReference(CollectionModel collection, RegistryNodeModel reference);

    void swapCollectionMemberships(Long collectionId, Long oneRefId, Long twoRefId);

    void mergeForGenerator(CollectionModel collection);

}
