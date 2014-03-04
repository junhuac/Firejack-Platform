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
Ext.define('OPF.console.domain.view.action.ActionParameterGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.action-parameters-grid',
    height: 240,
    cls: 'border-radius-grid-body border-radius-grid-docked-top',
    flex: 1,
    plugins: [
        new OPF.core.component.GridDragDropRowOrder({
            listeners: {
                afterrowmove: function(gridDropTarget) {
                    gridDropTarget.grid.refreshOrderPositions();
                }
            }
        })
    ],
    store: 'ActionParameters',
    columns: [
        OPF.Ui.populateColumn('name', 'Name', {width: 100}),
        OPF.Ui.populateColumn('location', 'Location', {width: 50}),
        OPF.Ui.populateColumn('fieldType', 'Type', {width: 70}),
        OPF.Ui.populateColumn('description', 'Description', {flex: 1, renderer: 'htmlEncode'}),
        OPF.Ui.populateHiddenColumn('orderPosition')
    ],

    initComponent: function() {
        this.tbar = [
            OPF.Ui.createBtn('Add', 50, 'add-parameter', {iconCls: 'silk-add'}),
            '-',
            OPF.Ui.createBtn('Delete', 60, 'delete-parameter', {iconCls: 'silk-delete'})
        ],

        this.callParent(arguments);
    },

    getEditorDialog: function() {
        var editorDialog = Ext.WindowMgr.get('actionParameterRowEditorDialog');
        if (OPF.isEmpty(editorDialog)) {
            editorDialog = Ext.create('OPF.console.domain.view.action.ActionParameterRowEditorDialog', this);
            Ext.WindowMgr.register(editorDialog);
        } else {
            editorDialog.setGrid(this);
        }
        return editorDialog;
    },

    cleanFieldStore: function() {
        this.store.removeAll();
    },

    refreshOrderPositions: function() {
        var records = this.store.getRange();
        Ext.each(records, function(rec, index) {
            rec.set('orderPosition', index + 1);
        });
    }
});

/**
 *
 */
Ext.define('OPF.console.domain.view.action.ActionParameterFieldSet', {
    extend: 'OPF.core.component.LabelContainer',
    alias: 'widget.action-parameters-fieldset',

    fieldLabel: 'Parameters',
    subFieldLabel: '',

    layout: 'anchor',

    editPanel: null,

    constructor: function(editPanel, cfg) {
        cfg = cfg || {};
        OPF.console.domain.view.action.ActionParameterFieldSet.superclass.constructor.call(
            this, Ext.apply({editPanel: editPanel}, cfg)
        );
    },

    initComponent: function() {
        this.inputVOParameterPanel = Ext.create('OPF.core.component.RegistryNodeDropPanel', {
            fieldLabel: 'Request Content [POST/PUT]',
            subFieldLabel: '',
            name: 'inputVOEntity',
            allowBlank: false,
            registryNodeType: OPF.core.utils.RegistryNodeType.ENTITY
        });
        this.outputVOParameterPanel = Ext.create('OPF.core.component.RegistryNodeDropPanel', {
            fieldLabel: 'Response Content [GET/POST/PUT/DELETE]',
            subFieldLabel: '',
            name: 'outputVOEntity',
            allowBlank: false,
            registryNodeType: OPF.core.utils.RegistryNodeType.ENTITY
        });
        this.actionParameterGrid = Ext.create('OPF.console.domain.view.action.ActionParameterGrid', {
            anchor: '100%',
            minHeight: 240
        });

        this.spacer = Ext.ComponentMgr.create({
            xtype: 'container',
            width: 500,
            height: 10
        });

        this.items = [
            this.inputVOParameterPanel,
            this.spacer,
            this.outputVOParameterPanel,
            {
                xtype: 'container',
                width: 500,
                height: 10
            },
            this.actionParameterGrid
        ];

        this.callParent(arguments);
    },

    initVOParameterPanel: function(jsonData) {
        if (isNotEmpty(jsonData)) {
            this.switchShowingVOParameterPanel(jsonData.method);
            if (isNotEmpty(jsonData.inputVOEntity)) {
                this.inputVOParameterPanel.setValue(jsonData.inputVOEntity.id);
                var iconCls = 'tricon-entity';
                if (jsonData.inputVOEntity.typeEntity == 'Classifier') {
                    iconCls = 'tricon-abstract-entity';
                } else if (jsonData.inputVOEntity.typeEntity == 'Data') {
                    iconCls = 'tricon-type-entity';
                }
                var description = cutting(jsonData.inputVOEntity.description, 70);
                this.inputVOParameterPanel.renderDraggableEntity(iconCls, jsonData.inputVOEntity.name, description, jsonData.inputVOEntity.lookup);
            }
            if (isNotEmpty(jsonData.outputVOEntity)) {
                this.outputVOParameterPanel.setValue(jsonData.outputVOEntity.id);

                var iconCls = 'tricon-entity';

                if (jsonData.outputVOEntity.type == 'ENTITY') {
                    if (jsonData.outputVOEntity.typeEntity == 'Classifier') {
                        iconCls = 'tricon-abstract-entity';
                    } else if (jsonData.outputVOEntity.typeEntity == 'Data') {
                        iconCls = 'tricon-type-entity';
                    }

                }
                else {
                    iconCls = 'tricon-'+jsonData.outputVOEntity.type.toLowerCase();
                }

                var description = cutting(jsonData.outputVOEntity.description, 70);
                this.outputVOParameterPanel.renderDraggableEntity(iconCls, jsonData.outputVOEntity.name, description, jsonData.outputVOEntity.lookup);
            }
        } else {
            this.switchShowingVOParameterPanel('GET');
        }
    },

    switchShowingVOParameterPanel: function(method) {
        if (method == 'GET' || method == 'DELETE') {
            this.inputVOParameterPanel.hide();
            this.spacer.hide();
        } else {
            this.inputVOParameterPanel.show();
            this.spacer.show();
            if (isEmpty(this.inputVOParameterPanel.getValue())) {
                var selectedNode = this.editPanel.managerLayout.navigationPanel.getSelectedNode();
                var selectedNodeType = OPF.core.utils.RegistryNodeType.findRegistryNodeByType(selectedNode.get('type'));
                if (OPF.core.utils.RegistryNodeType.ENTITY == selectedNodeType) {
                    this.inputVOParameterPanel.renderDraggableEntity(selectedNode);
                }
            }
        }
        if (isEmpty(this.outputVOParameterPanel.getValue())) {
            var selectedNode = this.editPanel.managerLayout.navigationPanel.getSelectedNode();
            var selectedNodeType = OPF.core.utils.RegistryNodeType.findRegistryNodeByType(selectedNode.get('type'));
            if (OPF.core.utils.RegistryNodeType.ENTITY == selectedNodeType) {
                this.outputVOParameterPanel.renderDraggableEntity(selectedNode);
            }
        }
    }

});