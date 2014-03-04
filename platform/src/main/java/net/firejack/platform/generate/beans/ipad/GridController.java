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

public class GridController implements iPad {
    private String suffix;
    private String name;
    private EditController edit;

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getName() {
        return name + suffix;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EditController getEdit() {
        return edit;
    }

    public void setEdit(EditController edit) {
        this.edit = edit;
    }

    @Override
    public String getFilePosition() {
        return FileUtils.construct("${name}", "controllers");
    }
}
