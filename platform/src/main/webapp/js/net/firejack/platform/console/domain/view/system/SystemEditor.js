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

/**
 *
 */
Ext.define('OPF.console.domain.view.system.SystemEditor', {
    extend: 'OPF.core.component.editor.BaseEditor',
    alias: 'widget.system-editor',

    title: 'SYSTEM: [New]',

    infoResourceLookup: 'net.firejack.platform.registry.registry-node.system',

    editableWithChild: true,

    /**
     *
     */
    initComponent: function() {
        var me = this;

        this.resetButton = Ext.ComponentMgr.create({
            xtype: 'button',
            cls: 'resetButton',
            disabled: true,
            text: 'Restart',
            action: 'reset'
        });

        this.additionalMainControls = [
            this.resetButton
        ];

        this.mainSystemDropPanel = Ext.create('OPF.core.component.RegistryNodeDropPanel', {
            fieldLabel: 'Main System',
            subFieldLabel: 'Drag and Drop from Cloud Navigator',
            name: 'main',
            registryNodeType: me.registryNodeType,
            renderDraggableEntityFromNode: function(model) {
                var instance = this;
                this.setValue(SqGetIdFromTreeEntityId(model.get('id')));
                var description = cutting(model.get('shortDescription'), 70);
                this.renderDraggableEntity('tricon-system', model.get('text'), description, model.get('lookup'));

                var mask = new Ext.LoadMask(me.getEl(), {msg: 'Loading Main System...'});
                mask.show();

                Ext.Ajax.request({
                    url: me.registryNodeType.generateGetUrl(this.getValue()),
                    method: 'GET',

                    success:function(response, action) {
                        mask.hide();
                        var vo = Ext.decode(response.responseText);
                        OPF.Msg.setAlert(OPF.Msg.STATUS_OK, vo.message);

                        var model = me.registryNodeType.createModel(vo.data[0]);
                        instance.renderDraggableEntity('tricon-system', model.get('name'), cutting(description, 400), model.get('lookup'));

                        me.resourceFields.serverNameField.setReadOnly(true);
                        me.resourceFields.portField.setReadOnly(true);
                        me.resourceFields.portField.setValue(model.get('port'));
                    },

                    failure:function(response) {
                        mask.hide();
                        var options = new OPF.core.validation.FormInitialisationOptions({
                            messageLevel: OPF.core.validation.MessageLevel.ERROR,
                            messagePanel: this.messagePanel
                        });
                        OPF.core.validation.FormInitialisation.showValidationMessages(response, me.form, options);
                    }
                });
            },
            listeners: {
                clear: function() {
                    me.resourceFields.serverNameField.setReadOnly(false);
                    me.resourceFields.portField.setReadOnly(false);
                }
            }
        });

        this.nodeBasicFields = Ext.create('OPF.core.component.editor.BasicInfoFieldSet', this);
        this.descriptionFields = Ext.create('OPF.core.component.editor.DescriptionInfoFieldSet', this, [ this.mainSystemDropPanel ]);
        this.resourceFields = Ext.create('OPF.core.component.editor.ResourceFieldSet', this, {
            needMethodField: false,
            needParentPathField: false,
            needUrlPathField: false,
            needProtocolField: false,

            updateResourceAccessFields: function() {
                this.updateUrlPath();
                this.updateResourceUrl();

                var lookup = me.nodeBasicFields.lookupField.getValue();
                var parts = lookup.split('.');
                parts = reverse(parts);
                lookup = parts.join('.');
                this.serverNameField.setValue(lookup);
            }
        });

        this.staticBlocks = [
            this.nodeBasicFields
        ];

        this.additionalBlocks = [
            this.descriptionFields,
            this.resourceFields
        ];

        this.callParent(arguments);
    },

    onBeforeSetValue: function(jsonData) {
        if (this.saveAs == 'update') {
            this.associatedPackagesFieldSet = Ext.create('OPF.console.domain.view.system.AssociatedPackagesFieldSet', this);
            this.fieldsPanel.add(this.associatedPackagesFieldSet);
            this.additionalBlocks.push(this.associatedPackagesFieldSet);
            this.rightNavContainer.update(this.prepareRightNavigation(this.additionalBlocks));
        }
    },

    onAfterSetValue: function(jsonData) {
        if (OPF.isNotEmpty(jsonData.main)) {
            this.mainSystemDropPanel.setValue(jsonData.main.id);
            var description = cutting(jsonData.main.description, 70);
            this.mainSystemDropPanel.renderDraggableEntity('tricon-system', jsonData.main.name, description, jsonData.main.lookup);
            this.resourceFields.serverNameField.setReadOnly(true);
            this.resourceFields.portField.setReadOnly(true);
        }

        if (OPF.isNotEmpty(jsonData) && OPF.isNotEmpty(jsonData.id) &&
            (!Ext.isString(jsonData.id) || (Ext.isString(jsonData.id) && OPF.isNotBlank(jsonData.id)))) {
            if (OPF.isNotEmpty(jsonData.associatedPackages)) {
                this.associatedPackagesFieldSet.associatedPackageGrid.store.loadData(jsonData.associatedPackages);
            }
            this.resetButton.setDisabled(false);
            this.resetButton.systemId = jsonData.id;
        }
    },

    onBeforeSave: function(formData) {
        var mainSystemId = this.mainSystemDropPanel.getValue();
        if (OPF.isNotEmpty(mainSystemId)) {
            formData.main = {
                id: mainSystemId
            }
        } else {
            delete formData.main;
        }
    },

    saveRequest: function(formData, url, method) {
        delete formData.parentPath;
        delete formData.protocol;
        delete formData.urlPath;

        this.callParent(arguments);
    },

    disableInstall: function(actionBtns) {
        Ext.get('btn-' + actionBtns[0]).hide();
        Ext.get('btn-' + actionBtns[1]).show();
    },

    enableInstall: function(actionBtns) {
        Ext.get('btn-' + actionBtns[0]).show();
        Ext.get('btn-' + actionBtns[1]).hide();
    }

});