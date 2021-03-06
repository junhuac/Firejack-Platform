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

package net.firejack.platform.core.store.registry;

import net.firejack.platform.core.model.SpecifiedIdsFilter;
import net.firejack.platform.core.model.registry.INavigable;
import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.domain.PackageModel;
import net.firejack.platform.core.model.registry.domain.SystemModel;
import net.firejack.platform.core.model.registry.system.DatabaseModel;
import net.firejack.platform.core.model.registry.system.FileStoreModel;
import net.firejack.platform.core.model.registry.system.ServerModel;
import net.firejack.platform.core.model.user.SystemUserModel;
import net.firejack.platform.core.store.user.ISystemUserStore;
import net.firejack.platform.core.utils.ClassUtils;
import net.firejack.platform.core.utils.Paging;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;


@Component("systemStore")
public class SystemStore extends RegistryNodeStore<SystemModel> implements ISystemStore {

    @Autowired
    @Qualifier("packageStore")
    private IPackageStore packageStore;
	@Autowired
	@Qualifier("registryNodeStore")
	private IRegistryNodeStore<RegistryNodeModel> registryNodeStore;

    @Autowired
    @Qualifier("databaseStore")
    private IDatabaseStore databaseStore;
    @Autowired
    @Qualifier("serverStore")
    private IServerStore serverStore;
    @Autowired
    @Qualifier("fileStore")
    private IFileStore fileStore;
    @Autowired
    @Qualifier("systemUserStore")
    private ISystemUserStore systemUserStore;

    /***/
    @PostConstruct
    public void init() {
        setClazz(SystemModel.class);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemModel findById(Long id) {
        SystemModel system = super.findById(id);
        if (system != null) {
            if (system.getMain() != null) {
                SystemModel mainSystem = (SystemModel) lazyInitializeIfNeed(system.getMain());
                Hibernate.initialize(mainSystem.getAssociatedPackages());
                system.setAssociatedPackages(mainSystem.getAssociatedPackages());
            } else {
                Hibernate.initialize(system.getAssociatedPackages());
            }
        }
        return system;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemModel> findAllByParentIdWithFilter(Long id, SpecifiedIdsFilter<Long> filter) {
        List<SystemModel> systems = super.findAllByParentIdWithFilter(id, filter);
        if (systems != null) {
            for (SystemModel system : systems) {
                Hibernate.initialize(system.getAssociatedPackages());
            }
        }
        return systems;
    }

	@Override
	@Transactional(readOnly = true)
	public SystemModel findByLookup(String lookup) {
		SystemModel model = super.findByLookup(lookup);
        if (model != null) {
            Hibernate.initialize(model.getAssociatedPackages());
        }
		return model;
	}

	@Override
    @Transactional(readOnly = true)
    public List<SystemModel> findAllByParentIdsWithFilter(List<Long> ids, SpecifiedIdsFilter<Long> filter, Paging paging) {
        List<SystemModel> systems = super.findAllByParentIdsWithFilter(ids, filter, paging);
        for (SystemModel system : systems) {
            Hibernate.initialize(system.getAssociatedPackages());
        }
        return systems;
    }

    @Override
    @Transactional
    public void delete(SystemModel system) {
        packageStore.removeAllAssociations(system);
        List<SystemUserModel> systemUserModels = systemUserStore.findBySystemId(system.getId());
        if (!systemUserModels.isEmpty()) {
            for (SystemUserModel systemUserModel : systemUserModels) {
                systemUserModel.setSystem(null);
            }
            systemUserStore.saveOrUpdateAll(systemUserModels);
        }
        super.delete(system);
    }

	@Override
	@Transactional
	public void save(SystemModel system) {
        boolean isNew = system.getId() == null;
        if (!isNew) {
			SystemModel systemModel = findById(system.getId());
            if (systemModel.getMain() == null) {
                List<PackageModel> associatedPackages = systemModel.getAssociatedPackages();
                if (associatedPackages != null) {
                    for (PackageModel model : associatedPackages) {
                        model.setServerName(system.getServerName());
                        model.setPort(system.getPort());

                        List<RegistryNodeModel> registryNodes = registryNodeStore.findAllByLikeLookupPrefix(model.getLookup() + ".");
                        for (RegistryNodeModel registryNode : registryNodes) {
                            if (registryNode instanceof INavigable) {
                                ((INavigable) registryNode).setServerName(system.getServerName());
                                ((INavigable) registryNode).setPort(system.getPort());
                            }
                        }
                    }
                }
            }
		}
		super.save(system);

        RegistryNodeModel mainSystemModel = system.getMain();
        if (isNew && mainSystemModel != null) {
            List<DatabaseModel> mainDatabaseModels = databaseStore.findAllByParentIdWithFilter(mainSystemModel.getId(), null);
            for (DatabaseModel mainDatabaseModel : mainDatabaseModels) {
                DatabaseModel databaseModel = new DatabaseModel();
                ClassUtils.copyProperties(mainDatabaseModel, databaseModel, new String[] { "id", "path", "lookup", "parent", "uid", "hash" });
                databaseModel.setParent(system);
                databaseModel.setMain(mainDatabaseModel);
                databaseStore.save(databaseModel);
            }

            List<ServerModel> mainServerModels = serverStore.findAllByParentIdWithFilter(mainSystemModel.getId(), null);
            for (ServerModel mainServerModel : mainServerModels) {
                ServerModel serverModel = new ServerModel();
                ClassUtils.copyProperties(mainServerModel, serverModel, new String[] { "id", "path", "lookup", "parent", "uid", "hash" });
                serverModel.setParent(system);
                serverModel.setMain(mainServerModel);
                serverStore.save(serverModel);
            }

            List<FileStoreModel> mainFilestoreModels = fileStore.findAllByParentIdWithFilter(mainSystemModel.getId(), null);
            for (FileStoreModel mainFilestoreModel : mainFilestoreModels) {
                FileStoreModel filestoreModel = new FileStoreModel();
                ClassUtils.copyProperties(mainFilestoreModel, filestoreModel, new String[] { "id", "path", "lookup", "parent", "uid", "hash" });
                filestoreModel.setParent(system);
                filestoreModel.setMain(mainFilestoreModel);
                fileStore.save(filestoreModel);
            }
        }

        if (!isNew && mainSystemModel == null) {
            List<SystemModel> aliasSystemModels = findAliasesById(system.getId(), null);
            for (SystemModel aliasSystemModel : aliasSystemModels) {
                synchronize(aliasSystemModel);
            }
        }
	}

    private void synchronize(SystemModel aliasSystemModel) {
        RegistryNodeModel mainSystemModel = aliasSystemModel.getMain();

        databaseStore.synchronize(mainSystemModel, aliasSystemModel);
        fileStore.synchronize(mainSystemModel, aliasSystemModel);
        serverStore.synchronize(mainSystemModel, aliasSystemModel);
    }

}
