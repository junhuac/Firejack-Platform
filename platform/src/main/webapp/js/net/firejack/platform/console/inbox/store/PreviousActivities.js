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

Ext.define('OPF.console.inbox.store.PreviousActivities', {
    extend: 'Ext.data.Store',
    restful: false,
    rollbackDialog: null,

    constructor: function(rollbackDialog, cfg) {
        cfg = cfg || {};
        OPF.console.inbox.store.PreviousActivities.superclass.constructor.call(this, Ext.apply({
            model: 'OPF.console.domain.model.ActivityModel',
            proxy: {
                type: 'ajax',
                url: OPF.core.utils.RegistryNodeType.ACTIVITY.generateUrl('/process/'),
                method: 'GET',
                reader: {
                    type: 'json',
                    successProperty: 'success',
                    idProperty: 'id',
                    root: 'data',
                    messageProperty: 'message',
                    totalProperty: 'total'
                }
            },
            rollbackDialog: rollbackDialog
        }, cfg));
    },

    reloadStore: function() {
        var taskCaseId = this.rollbackDialog.objectIdField.getValue();
        var url = OPF.core.utils.RegistryNodeType.ACTIVITY.generateUrl('/previous?' +
            (this.rollbackDialog.isCaseDialog ? 'caseId=' : 'taskId=') + taskCaseId);
        var me = this;
        Ext.Ajax.request({
            url: url,
            method: 'GET',
            success: function(response, action) {
                var resp = Ext.decode(response.responseText);
                if (resp.success) {
                    if (resp.data == null || resp.data.length == 0) {
                        me.rollbackDialog.previousActivitiesField.hide();
                    } else {
                        me.rollbackDialog.previousActivitiesField.store.loadData(resp.data);
                        me.rollbackDialog.previousActivitiesField.setValue(resp.data[0]);
                        me.rollbackDialog.previousActivitiesField.show();
                    }
                } else {
                    Ext.Msg.alert('Error', resp.message);
                    me.rollbackDialog.previousActivitiesField.hide();
                }
            }
        });
    }
});