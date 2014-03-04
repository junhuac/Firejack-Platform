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

Ext.define('OPF.console.inbox.store.CaseAttachments', {
    extend: 'Ext.data.Store',

    restful: false,
    autoLoad: false,
    constructor: function(cfg) {
        cfg = cfg || {};
        OPF.console.inbox.store.CaseAttachments.superclass.constructor.call(this, Ext.apply({
            model: 'OPF.console.inbox.model.CaseAttachmentModel',
            proxy: {
                type: 'ajax',
                url: OPF.core.utils.RegistryNodeType.CASE_ATTACHMENT.generateUrl(),
                method: 'GET',
                reader: {
                    type: 'json',
                    successProperty: 'success',
                    idProperty: 'id',
                    root: 'data',
                    messageProperty: 'message',
                    totalProperty: 'total'
                }
            }
        }, cfg));
    }
});