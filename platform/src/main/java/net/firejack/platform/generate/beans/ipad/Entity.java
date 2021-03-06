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

package net.firejack.platform.generate.beans.ipad;

import net.firejack.platform.core.utils.FileUtils;
import net.firejack.platform.core.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.firejack.platform.api.registry.model.FieldType.NAME;

public class Entity implements iPad {
    private String prefix;
    private String name;
    private String lookup;
    private String heading;
    private String subHeading;
    private String description;
    private boolean report;
    private Entity extend;
    private GridController grid;
    private EditController edit;
    private List<Property> properties = new ArrayList<Property>();
    private List<ClientMethod> methods = new ArrayList<ClientMethod>();
    private Set<String> imports = new HashSet<String>();
    private Set<String> classes = new HashSet<String>();

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return StringUtils.isNotBlank(prefix) ? (StringUtils.capitalize(prefix) + name) : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getSubHeading() {
        return subHeading;
    }

    public void setSubHeading(String subHeading) {
        this.subHeading = subHeading;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isReport() {
        return report;
    }

    public void setReport(boolean report) {
        this.report = report;
    }

    public Entity getExtend() {
        return extend;
    }

    public void setExtend(Entity extend) {
        this.extend = extend;
    }

    public GridController getGrid() {
        return grid;
    }

    public void setGrid(GridController grid) {
        this.grid = grid;
    }

    public EditController getEdit() {
        return edit;
    }

    public void setEdit(EditController edit) {
        this.edit = edit;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void addProperty(Property property) {
        this.properties.add(property);
    }

    public List<ClientMethod> getMethods() {
        return methods;
    }

    public void addMethods(ClientMethod method) {
        this.methods.add(method);
    }

    public Set<String> getImports() {
        return imports;
    }

    public void addImport(Entity header) {
        String name = header.getName();
        if (!this.name.equals(name)) {
            this.imports.add(name);
        }
    }

    public void addImport(String entity) {
        if (!this.name.equals(entity))
            this.imports.add(entity);
    }

    public String getClassesString() {
        return StringUtils.join(classes, ", ");
    }

    public Set<String> getClasses() {
        return classes;
    }

    public void addClass(Entity header) {
        String name = header.getName();
        if (!this.name.equals(name)) {
            this.classes.add(name);
        }
    }

    public void addClass(String entity) {
        if (!this.name.equals(entity))
            this.classes.add(entity);
    }

    public boolean isEmptyReferenceObject() {
        return StringUtils.isEmpty(heading) && StringUtils.isEmpty(subHeading) && StringUtils.isEmpty(description);
    }

    public String getDisplayName() {
        String displayName = "pk";
        for (Property property : properties) {
            if (property.getFieldType() == NAME) {
                displayName = property.getName();
                break;
            }
            if (displayName.equals("pk") && property.getFieldType() != null && property.getFieldType().isString())
                displayName = property.getName();
        }

        return displayName;
    }

    @Override
    public String getFilePosition() {
        return FileUtils.construct("${name}", "api", "bean", "${name}");
    }
}
