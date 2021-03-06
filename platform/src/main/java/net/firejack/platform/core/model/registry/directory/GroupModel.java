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

package net.firejack.platform.core.model.registry.directory;

import net.firejack.platform.core.model.registry.IAllowCreateAutoDescription;
import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.RegistryNodeType;
import net.firejack.platform.core.model.registry.authority.RoleModel;
import net.firejack.platform.core.model.registry.process.ActorModel;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.util.List;


@Entity
@DiscriminatorValue("GRP")
public class GroupModel extends RegistryNodeModel implements IAllowCreateAutoDescription {

    private static final long serialVersionUID = 7013150861920728169L;
    private DirectoryModel directory;

    private List<ActorModel> actors;
    private List<RoleModel> roles;

    /**
     * @return
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_directory")
    @ForeignKey(name = "FK_DIRECTORY_GROUP")
    public DirectoryModel getDirectory() {
        return directory;
    }

    /**
     * @param directory
     */
    public void setDirectory(DirectoryModel directory) {
        this.directory = directory;
    }

    @Override
    @Transient
    public RegistryNodeType getType() {
        return RegistryNodeType.GROUP;
    }

    /**
     * @return
     */
    @ManyToMany(targetEntity = ActorModel.class, fetch = FetchType.LAZY)
    @JoinTable(
            name = "opf_actor_group",
            joinColumns = @JoinColumn(name = "id_group", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_actor", referencedColumnName = "id")
    )
    @ForeignKey(name = "FK_ROLE_PERMISSIONS")
    public List<ActorModel> getActors() {
        return actors;
    }

    /**
     * @param actors
     */
    public void setActors(List<ActorModel> actors) {
        this.actors = actors;
    }

    @ManyToMany(targetEntity = RoleModel.class, fetch = FetchType.LAZY)
    @JoinTable(
            name = "opf_group_role",
            joinColumns = @JoinColumn(name = "id_group", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_role", referencedColumnName = "id")
    )
    @ForeignKey(name = "FK_GROUP_ROLES")
    public List<RoleModel> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleModel> roles) {
        this.roles = roles;
    }

}