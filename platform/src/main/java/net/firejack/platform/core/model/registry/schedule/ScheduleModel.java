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

package net.firejack.platform.core.model.registry.schedule;


import net.firejack.platform.core.annotation.Lookup;
import net.firejack.platform.core.model.registry.LookupModel;
import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.RegistryNodeType;
import net.firejack.platform.core.model.registry.domain.ActionModel;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

@Entity
@Lookup(parent = RegistryNodeModel.class)
@Table(name = "opf_schedule")
public class ScheduleModel extends LookupModel<RegistryNodeModel> {
    private static final long serialVersionUID = 6412443928883180524L;

    private ActionModel action;

    private String cronExpression;

    // name and email for technical assistance on scheduled item

    // email address for failure notification
    private String emailFailure;

    private boolean active;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_action")
    @ForeignKey(name = "fk_schedule_action")
    public ActionModel getAction() {
        return action;
    }

    public void setAction(ActionModel action) {
        this.action = action;
    }

    @Column(length = 32)
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Column(length = 255)
    public String getEmailFailure() {
        return emailFailure;
    }

    public void setEmailFailure(String emailFailure) {
        this.emailFailure = emailFailure;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    @Transient
    public RegistryNodeType getType() {
        return RegistryNodeType.SCHEDULE;
    }
}
