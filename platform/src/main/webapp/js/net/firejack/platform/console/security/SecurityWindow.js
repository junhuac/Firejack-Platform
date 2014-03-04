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

Ext.define('OPF.console.security.SecurityWindow', {
    extend: 'Ext.window.Window',

    title: 'Secure Object:',
    id: 'FJK-opf-clds-ui-secwin',
    width: 755,
    height: 380,
    layout: {
        type: 'hbox',
        align: 'stretch',
        padding: 5
    },
    modal: true,

    managerLayout: null,
    selectedDirectoryId: null,

    selectedAssignedUserId: null,

    constructor: function(managerLayout, cfg) {
        cfg = cfg || {};
        OPF.console.security.SecurityWindow.superclass.constructor.call(this, Ext.apply({
            managerLayout: managerLayout
        }, cfg));
    },

    initComponent: function() {
        var instance = this;

        this.searchField = Ext.create('OPF.core.component.TextField', {
            width: 120,
            enableKeyEvents: true,
            listeners: {
                change: function(cmp, newValue, oldValue) {
                    instance.refreshFields();
                },
                keyup: function(cmp, e) {
                    instance.refreshFields();
                }
            }
        });

        this.directoryStore = Ext.create('OPF.console.security.DirectoryStore');

        this.directoriesField = OPF.Ui.comboFormField('directory', 'Directory', {
            width: 170,
            labelWidth: 60,
            selectOnFocus: true,
            editable: false,
            typeAhead: true,
            triggerAction: 'all',
            lazyRender:true,
            queryMode: 'remote',
            store: this.directoryStore,
            valueField: 'id',
            displayField: 'name',
            value: 'All Directories',
            listeners: {
                select: function(cmp, e) {
                    instance.selectedDirectoryId = cmp.getValue();
                    instance.refreshFields();
                }
            }
        });

        /**
         * The store for the list of known users in a particular directory.
         */
        this.knownUserStore = Ext.create('OPF.console.security.KnownUserStore', this);

        this.knownUserGrid = Ext.create('Ext.grid.Panel', {
            title: 'Known Users',
            flex: 1,
            store: this.knownUserStore,
            columns: [
                OPF.Ui.populateColumn('username', 'Username', {width: 80}),
                OPF.Ui.populateColumn('email', 'Email', {width: 80}),
                OPF.Ui.populateColumn('firstName', 'First Name', {width: 80}),
                OPF.Ui.populateColumn('lastName', 'Last Name', {width: 80})
            ],

            tbar: {
                xtype: 'toolbar',
                items: [
                    this.searchField,
                    {
                        xtype: 'button',
                        text: 'clear',
                        handler: function (btn) {
                            instance.searchField.setValue('');
                            instance.refreshFields();
                        }
                    },
                    {xtype: 'tbfill'},
                    this.directoriesField
                ]
            },
            listeners: {
                itemdblclick: function(view, model, htmlElement, rowIndex, e, eOpts) {
                    instance.selectedAssignedUserId = model.get('id');
                    if (instance.assignedUserStore.getById(instance.selectedAssignedUserId) == null) {
                        var assignedUserModel = Ext.create('OPF.console.directory.model.UserModel');
                        assignedUserModel.set('id', model.get('id'));
                        assignedUserModel.set('username', model.get('username'));
                        assignedUserModel.set('email', model.get('email'));
                        assignedUserModel.set('firstName', model.get('firstName'));
                        assignedUserModel.set('lastName', model.get('lastName'));

                        instance.assignedUserStore.add(assignedUserModel);
                        instance.assignedUserGrid.getSelectionModel().select([ assignedUserModel ]);
                    }
                    var availableRoleModel = instance.availableRoleStore.getAt(0);
                    instance.saveRoleAssignment(availableRoleModel, true);
                }
            }
        });

        /**
         * The list of assigned and available roles for the entity being secured
         * at present and the known user.
         */
        this.assignedUserStore = Ext.create('OPF.console.security.AssignedUserStore', this);

        this.assignedUserGrid = Ext.create('Ext.grid.Panel', {
            title: 'Assigned Users',
            flex: 1,
            store: this.assignedUserStore,
            columns: [
                OPF.Ui.populateColumn('username', 'Username', {width: 80}),
                OPF.Ui.populateColumn('email', 'Email', {width: 80}),
                OPF.Ui.populateColumn('firstName', 'First Name', {width: 80}),
                OPF.Ui.populateColumn('lastName', 'Last Name', {width: 80})
            ],
            listeners: {
                itemclick: function(view, model, htmlElement, rowIndex, e, eOpts) {
                    instance.selectedAssignedUserId = model.get('id');
                    instance.availableRoleStore.load();
                }
            }
        });

        this.availableRoleStore = Ext.create('OPF.console.security.AvailableRolesStore', this);

        this.availableRolesGrid = Ext.create('Ext.grid.Panel', {
            title: 'Available Roles',
            flex: 1,
            store: this.availableRoleStore,
            columns: [
                OPF.Ui.populateCheckBoxColumn('assigned', 'Assigned', 60),
                OPF.Ui.populateColumn('name', 'Role', {flex: 1})
            ]
        });

        this.items = [
            this.knownUserGrid,
            OPF.Ui.xSpacer(5),
            {
                xtype: 'panel',
                flex: 1,
                border: false,
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    this.assignedUserGrid,
                    this.availableRolesGrid
                ]
            }
        ];

        var that = this;

        this.fbar = {
            xtype: 'toolbar',
            items: [
                {
                    xtype: 'button',
                    text: 'Close',
                    handler: function() {
                        that.close();
                    }
                }
            ]
        };

        this.callParent(arguments);
    },

    listeners: {
        show: function(dialog) {
            var selectedModel = dialog.getSelectedNode();
            dialog.setTitle('Secure Object: [' + selectedModel.get('type') + ': ' + selectedModel.get('text') + ']');
            dialog.knownUserStore.load();
            dialog.assignedUserStore.load();
            dialog.availableRoleStore.load();
        }
    },

    refreshFields: function(selectedNode) {
        this.knownUserStore.load();
    },

    getSelectedNodeId: function() {
        var selectedModel = this.getSelectedNode();
        return SqGetIdFromTreeEntityId(selectedModel.get('id'));
    },

    getSelectNodeEntityType: function() {
        var selectedModel = this.getSelectedNode();
        return selectedModel.get('entityType');
    },

    getSelectedNode: function() {
        return this.managerLayout.navigationPanel.getSelectedNode();
    },

    saveRoleAssignment: function(availableRoleModel, assigned) {
        if (isNotEmpty(availableRoleModel)) {
            var url = OPF.console.security.AssignedRoleModel.getAssignmentRoleUrl(this, true);
            url += '?assigned=' + assigned;
            url += '&roleId=' + availableRoleModel.get('id');
            var instance = this;
            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                success:function(response, action) {
                    var resp = Ext.decode(response.responseText);
                    OPF.Msg.setAlert(true, resp.message);
                    instance.availableRoleStore.load();
                },
                failure:function(response) {
                    OPF.core.validation.FormInitialisation.showValidationMessages(response, null);
                }
            });
        }
    }

});