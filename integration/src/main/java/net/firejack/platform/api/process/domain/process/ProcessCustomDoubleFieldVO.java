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

package net.firejack.platform.api.process.domain.process;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class represents value object for a process custom field with Double value.
 * It derives from <code>net.firejack.platform.api.process.domain.process.ProcessCustomFieldVO</code>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ProcessCustomDoubleFieldVO extends ProcessCustomFieldVO {

	private static final long serialVersionUID = 4058606682425887077L;

	/** Constructor without the arguments */
	public ProcessCustomDoubleFieldVO() {
	}

	/**
	 * Constructor setting the field lookup and value
	 *
	 * @param fieldLookup lookup of the process custom field
	 * @param value       Double value of the process custom field
	 */
	public ProcessCustomDoubleFieldVO(String fieldLookup, Double value) {
		this.setFieldLookup(fieldLookup);
		this.setValue(value);
	}

	/**
	 * Gets the value
	 *
	 * @return Double value of the process custom field
	 */
	@XmlElement(name = "doubleValue")
	public Double getValue() {
		return (Double) value;
	}

	/**
	 * Sets the value
	 *
	 * @param value Double value of the process custom field
	 */
	public void setValue(Double value) {
		this.value = value;
	}

}