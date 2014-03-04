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

package net.firejack.platform.api.registry.field;

import net.firejack.platform.api.registry.model.FieldType;
import net.firejack.platform.core.annotation.Property;
import net.firejack.platform.core.domain.Lookup;
import net.firejack.platform.core.utils.MessageResolver;
import net.firejack.platform.core.validation.annotation.*;
import net.firejack.platform.core.validation.constraint.RuleSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Locale;

@Component
@XmlRootElement
@RuleSource("OPF.registry.Field")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Field extends Lookup {
	private static final long serialVersionUID = 2293874598687519953L;

    @Property
    private FieldType fieldType;
    @Property
    private String customFieldType;
    @Property
    private Boolean required;
	@Property
    private Boolean searchable;
    @Property
    private Boolean autoGenerated;
    private Boolean inherited;
    @Property
    private String defaultValue;
	@Property
    private String displayName;
	@Property
    private String displayDescription;
    @Property(name = "allowedFieldValueList.entries")
    private List<String> allowedValues;

    @Override
    @NotBlank
    @Length(maxLength = 255)
    @Match(expression = "^[a-z]([a-z0-9]*_?[a-z0-9]+)*$", msgKey = "validation.parameter.field.name.should.match.exp")
    @NotMatch(javaWords = false)
    public String getName() {
        return super.getName();
    }

    @NotNull
    @EnumValue(enumClass = FieldType.class, hasName = true, hasDescription = true)
    @DefaultValue("UNIQUE_ID")
    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldTypeName() {
        return MessageResolver.messageFormatting("net.firejack.platform.api.registry.model.FieldType." + getFieldType().name() + ".name", Locale.ENGLISH);
    }

    @Length(maxLength = 1024)
    public String getCustomFieldType() {
        return customFieldType;
    }

    public void setCustomFieldType(String customFieldType) {
        this.customFieldType = customFieldType;
    }

    @NotNull
    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    public Boolean getAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(Boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    @Length(maxLength = 255)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getInherited() {
        return inherited;
    }

    public void setInherited(Boolean inherited) {
        this.inherited = inherited;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Length(maxLength = 255)
    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    @XmlElementWrapper(name = "allowedValues")
    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

}
