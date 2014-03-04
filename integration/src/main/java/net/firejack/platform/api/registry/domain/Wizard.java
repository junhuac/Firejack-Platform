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

package net.firejack.platform.api.registry.domain;


import net.firejack.platform.core.annotation.Property;
import net.firejack.platform.core.domain.Lookup;
import net.firejack.platform.core.validation.annotation.Length;
import net.firejack.platform.core.validation.annotation.Match;
import net.firejack.platform.core.validation.annotation.NotBlank;
import net.firejack.platform.core.validation.annotation.Validate;
import net.firejack.platform.core.validation.constraint.RuleSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static net.firejack.platform.core.validation.annotation.DomainType.*;

@Component
@XmlRootElement
@RuleSource("OPF.registry.Wizard")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@XmlAccessorType(XmlAccessType.FIELD)
@Validate(type = WIZARD, parents = {DOMAIN}, unique = {ENTITY, REPORT, WIZARD})
public class Wizard extends Lookup {

    @Property
    @XmlElementWrapper(name = "fields")
    private List<WizardField> fields;

    @Property
    private Lookup main;

    @Override
    @NotBlank
    @Length(maxLength = 255)
    @Match(expression = "^([A-Z][a-z0-9]*)(\\s([A-Z][a-z0-9]*))*$", msgKey = "validation.parameter.report.name.should.match.exp")
    public String getName() {
        return super.getName();
    }

    public List<WizardField> getFields() {
        return fields;
    }

    public void setFields(List<WizardField> fields) {
        this.fields = fields;
    }

    public Lookup getMain() {
        return main;
    }

    public void setMain(Lookup main) {
        this.main = main;
    }
}
