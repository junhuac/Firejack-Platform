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

package net.firejack.platform.api.authority.domain;

import net.firejack.platform.core.domain.AbstractDTO;
import net.firejack.platform.core.domain.IdFilter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


@Component
@XmlRootElement
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@XmlAccessorType(XmlAccessType.FIELD)
public class TypeFilter extends AbstractDTO {
	private static final long serialVersionUID = 4206124757492110009L;
	private String type;
    private IdFilter idFilter;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public IdFilter getIdFilter() {
        return idFilter;
    }

    public void setIdFilter(IdFilter idFilter) {
        this.idFilter = idFilter;
    }
}