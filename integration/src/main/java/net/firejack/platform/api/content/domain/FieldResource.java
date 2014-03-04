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

package net.firejack.platform.api.content.domain;

import net.firejack.platform.api.registry.field.Field;
import net.firejack.platform.core.domain.AbstractDTO;
import net.firejack.platform.core.validation.constraint.RuleSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RuleSource("OPF.content.FieldResource")
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class FieldResource extends AbstractDTO {
	private static final long serialVersionUID = 1243287958307743702L;

	private Field field;
    private AbstractResource resource;

    /**
     * @return
     */
    public Field getField() {
        return field;
    }

    /**
     * @param field
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * @return
     */
    public AbstractResource getResource() {
        return resource;
    }

    /**
     * @param resource
     */
    public void setResource(AbstractResource resource) {
        this.resource = resource;
    }
}
