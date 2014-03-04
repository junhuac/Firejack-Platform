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

package net.firejack.platform.generate.beans.web.model.key;


import net.firejack.platform.api.registry.model.FieldType;
import net.firejack.platform.generate.beans.Import;

public class FieldKey implements Key {
    private FieldType type;

    public FieldKey() {
    }

    public FieldKey(FieldType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return type.getClassName();
    }

    public FieldType getType() {
        return type;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public String getPackage() {
        return null;
    }

    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public int compareTo(Import o) {
        return 0;
    }
}
