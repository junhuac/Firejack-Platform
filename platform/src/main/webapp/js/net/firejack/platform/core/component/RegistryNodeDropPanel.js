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

//@tag opf-console


/**
 *
 */
Ext.define('OPF.core.component.RegistryNodeDropPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.registry-node-drop-panel',

    layout: 'anchor',
    cls: 'registry-node-drop-panel',

    fieldLabel: null,
    subFieldLabel: null,
    border: false,

    registryNodeType: null,

    readOnly: false,
    data: null,

    allowDelete: true,
    allowBlank: true,
    errorMessage: '{0} is required',

    initComponent: function() {
        var me = this;

        this.addEvents(
            'beforenotifydrop',
            'afternotifydrop',
            'clear'
        );

        this.voTemplate = new Ext.XTemplate(
            '<div class="vo-drop-panel">' +
                '<div class="vo-info">' +
                    '<div class="vo-name">' +
                        '<div class="vo-icon {iconCls}"></div>' +
                        '<span>{name}</span>' +
                    '</div>' +
                    '<div class="vo-lookup">{lookup}</div>' +
                '</div>' +
                '<div class="vo-description">{description}</div>' +
            '</div>'
        );

        this.label = Ext.ComponentMgr.create({
            xtype: 'container',
            anchor: '100%',
            html:
                '<label class="x-form-item-label x-form-item-label-top">' +
                    '<span class="main-label">' + this.fieldLabel + ':</span>' +
                    '<span class="sub-label ">' + this.subFieldLabel + '</span>' +
                '</label>',
            margin: '0 0 5 0'
        });

        this.dropContainer = Ext.ComponentMgr.create({
            xtype: 'container',
            height: 50,
            anchor: '100%',
            cls: 'border-radius',
            border: false,
            tpl: this.voTemplate,
            listeners: {
                render: function(component) {
                    component.getEl().on('click', function() {
                        me.draggableNodeIdField.validate();
                    });
                }
            }
        });

        this.draggableNodeIdField = Ext.ComponentMgr.create({
            xtype: 'opf-hidden',
            name: this.name,
            allowBlank: this.allowBlank,
            blankText: Ext.String.format(this.errorMessage, this.fieldLabel),
            listeners: {
                errorchange: function(field, error) {
                    if (me.dropContainer.getEl()) {
                        if (OPF.isNotBlank(error)) {
                            me.dropContainer.addCls('opf-drop-container-invalid');
                            me.dropContainer.addCls('x-form-invalid-field');
                            me.dropContainer.el.dom.setAttribute('data-errorqtip', error || '');
                        } else {
                            me.dropContainer.removeCls('opf-drop-container-invalid');
                            me.dropContainer.removeCls('x-form-invalid-field');
                            me.dropContainer.el.dom.removeAttribute('data-errorqtip');
                        }
                    }
                }
            },
            setDefaultValue: function(defaultValue) {
                me.setDefaultValue(defaultValue);
            }
        });

        this.items = [
            this.label,
            this.dropContainer,
            this.draggableNodeIdField
        ];

        if (this.allowDelete) {
            this.cleanButton = Ext.ComponentMgr.create({
                xtype: 'button',
                iconCls: 'silk-delete',
                handler: me.clearDraggableEntity,
                scope: this,
                hidden: true
            });

            this.dockedItems = [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    ui: 'footer',
                    items: [
                        '->',
                        this.cleanButton
                    ]
                }
            ];
        }

        this.callParent(arguments);
    },

    listeners: {
        disable: function(panel) {
            panel.draggableNodeIdField.allowBlank = true;
            panel.draggableNodeIdField.disable();
            panel.draggableNodeIdField.validate();
        },
        enable: function(panel) {
            panel.draggableNodeIdField.allowBlank = panel.allowBlank;
            panel.draggableNodeIdField.enable();
            panel.draggableNodeIdField.validate();
        },
        hide: function(panel) {
            panel.draggableNodeIdField.allowBlank = true;
            panel.draggableNodeIdField.disable();
            panel.clearDraggableEntity();
            panel.draggableNodeIdField.validate();
        },
        show: function(panel) {
            panel.draggableNodeIdField.allowBlank = panel.allowBlank;
            panel.draggableNodeIdField.enable();
            panel.draggableNodeIdField.validate();
        }
    },

    onRender: function(ct, position) {
        var me = this;
        this.callParent(arguments);

        var sourcePanelDropTarget = new Ext.dd.DropTarget(this.body.dom, {
            ddGroup: 'cloudNavigatorDDGroup',
            notifyEnter: function(ddSource, e, data) {
                if (me.notifyEnterValidation(data.records[0])) {
                    me.body.highlight();
                } else {
                    this.lock();
                    setTimeout(function() {
                        sourcePanelDropTarget.unlock();
                    }, 200);
                }
            },
            notifyDrop: function(ddSource, e, data){
                var model = data.records[0];
                if (me.notifyEnterValidation(model)) {
                    if (me.fireEvent('beforenotifydrop', this, model, data) !== false) {
                        model = me.renderDraggableEntityFromNode(model);
                        if (me.fireEvent('afternotifydrop', this, model, data) !== false) {
                            OPF.Msg.setAlert(true, 'Added!!!');
                            return true;
                        }
                    }
                }
                OPF.Msg.setAlert('Warning',"'" + model.get('text') + "' can't be add.");
                return false;
            }
        });
    },

    notifyEnterValidation: function(model) {
        var elementType = model.get('type');
        var sourceType = OPF.core.utils.RegistryNodeType.findRegistryNodeByType(elementType);
        return this.registryNodeType == sourceType && !this.readOnly;
    },

    renderDraggableEntityFromNode: function(model) {
        this.draggableNodeIdField.setValue(SqGetIdFromTreeEntityId(model.get('id')));
        var description = cutting(model.get('shortDescription'), 70);
        var lookup = model.get('lookup');
        this.renderDraggableEntity(model.get('iconCls'), model.get('text'), description, lookup);
        return model;
    },

    renderDraggableEntity: function(iconCls, name, description, lookup) {
        this.data = {
            iconCls: iconCls,
            name: name,
            description: OPF.ifBlank(description, ''),
            lookup: OPF.ifBlank(lookup, '')
        };
        this.setData(this.data);
        if (this.allowDelete && !this.readOnly) {
            this.cleanButton.show();
        }
    },

    getValue: function() {
        return OPF.ifBlank(this.draggableNodeIdField.getValue(), null) ;
    },

    setValue: function(value) {
        return this.draggableNodeIdField.setValue(value);
    },

    getData: function() {
        return this.data;
    },

    setData: function(data) {
        this.dropContainer.update(data);
        if (this.allowDelete && !this.readOnly) {
            this.cleanButton.show();
        }
    },

    setDefaultValue: function(defaultValue) {
    },

    setReadOnly: function(readOnly) {
        this.readOnly = readOnly;
    },

    clearDraggableEntity: function() {
        if (this.fireEvent('clear', this) !== false) {
            this.data = null;
            this.dropContainer.update('');
            this.draggableNodeIdField.setValue(null);
            if (this.allowDelete && !this.readOnly) {
                this.cleanButton.hide();
            }
        }
    }

});
