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

package net.firejack.platform.model.upgrader.operator.bean;

import net.firejack.platform.api.registry.model.RelationshipOption;
import net.firejack.platform.model.upgrader.bean.INamedUpgradeModel;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for add-foreign-keyType complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="add-foreign-keyType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attribute name="table" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "add-foreign-keyType", propOrder = {
        "value"
})
public class AddForeignKeyType implements INamedUpgradeModel {

    @XmlValue
    protected String value;
    @XmlAttribute
    protected String table;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String field;
    @XmlAttribute(name = "reference-table")
    protected String referenceTable;
    @XmlAttribute(name = "reference-field")
    protected String referenceField;
    @XmlAttribute(name = "on-delete")
    protected RelationshipOption onDelete;
    @XmlAttribute(name = "on-update")
    protected RelationshipOption onUpdate;


    /**
     * Gets the value of the value property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the table property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getTable() {
        return table;
    }

    /**
     * Sets the value of the table property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setTable(String value) {
        this.table = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * @return
     */
    public String getField() {
        return field;
    }

    /**
     * @param field
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * @return
     */
    public String getReferenceTable() {
        return referenceTable;
    }

    /**
     * @param referenceTable
     */
    public void setReferenceTable(String referenceTable) {
        this.referenceTable = referenceTable;
    }

    /**
     * @return
     */
    public String getReferenceField() {
        return referenceField;
    }

    /**
     * @param referenceField
     */
    public void setReferenceField(String referenceField) {
        this.referenceField = referenceField;
    }

    /**
     * @return
     */
    public RelationshipOption getOnDelete() {
        return onDelete;
    }

    /**
     * @param onDelete
     */
    public void setOnDelete(RelationshipOption onDelete) {
        this.onDelete = onDelete;
    }

    /**
     * @return
     */
    public RelationshipOption getOnUpdate() {
        return onUpdate;
    }

    /**
     * @param onUpdate
     */
    public void setOnUpdate(RelationshipOption onUpdate) {
        this.onUpdate = onUpdate;
    }
}
