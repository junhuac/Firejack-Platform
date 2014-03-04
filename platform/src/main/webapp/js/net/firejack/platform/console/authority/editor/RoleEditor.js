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

Ext.ns('FJK.platform.clds.ui');

/**
 *
 */
Ext.define('OPF.console.authority.editor.RoleEditor', {
    extend: 'OPF.core.component.editor.BaseSupportedPermissionEditor',

    title: 'ROLE: [New]',

    infoResourceLookup: 'net.firejack.platform.authority.role',

    hideEditPanel: function() {
        OPF.console.authority.editor.RoleEditor.superclass.hideEditPanel.call(this);
        this.managerLayout.tabPanel.setActiveTab(this.managerLayout.roleGridView);
    },

    onSuccessSaved: function(method, vo) {
    }
    
});


