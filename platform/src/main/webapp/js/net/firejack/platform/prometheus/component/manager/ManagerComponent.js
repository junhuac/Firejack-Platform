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

Ext.require([
    'OPF.prometheus.component.manager.GridComponent',
    'OPF.prometheus.component.manager.FormComponent',
    'OPF.prometheus.component.manager.WorkflowFormComponent'
]);

Ext.define('OPF.prometheus.component.manager.ManagerComponent', {
    extend: 'Ext.container.Container',
    alias: 'widget.prometheus.component.manager-component',

    model: null,
    border: false,
    margin: '0 5 0 0',

    // private variable
    securityEnabled: false,

    configs: {
        formPanel: null,
        gridPanel: null
    },

    constructor: function(cfg) {
        cfg = cfg || {};
        cfg.configs = cfg.configs || {};
        cfg.configs = Ext.Object.merge(this.configs, cfg.configs);

        this.superclass.constructor.call(this, cfg);
    },

    initComponent: function() {
        var me = this;

        var isShowEditor = this.isShowEditor();

        /*this.parentLinksContainer = Ext.create('Ext.container.Container', {
            html: ''
        });*/

        if (this.isWorkflow()) {
            this.formPanel = Ext.create('OPF.prometheus.component.manager.WorkflowFormComponent', {
                managerPanel: this,
                model: this.model,
                configs: this.configs.formPanel
            });

            this.items = [
                this.formPanel
            ];
        } else {
            this.gridPanel = Ext.create('OPF.prometheus.component.manager.GridComponent', {
                managerPanel: this,
                model: this.model,
                configs: this.configs.gridPanel,
                hidden: isShowEditor,
                reportMode: false
            });

            this.formPanel = Ext.create('OPF.prometheus.component.manager.FormComponent', {
                managerPanel: this,
                model: this.model,
                hidden: !isShowEditor,
                configs: this.configs.formPanel
            });

            this.items = [
                //this.parentLinksContainer,
                this.gridPanel,
                this.formPanel
            ];
        }

        this.callParent(arguments);
    },

    listeners: {
        afterrender: function() {
            this.prepareEntityEditor();
        }
    },

    isWorkflow: function() {
        var process = OPF.Cfg.EXTRA_PARAMS.process;
        return OPF.isNotEmpty(process);
    },

    isShowEditor: function() {
        var entityId = OPF.getQueryParam("entityId");

        return (OPF.isNotEmpty(entityId) && Ext.isNumeric(entityId)) || this.isWorkflow();
    },

    prepareEntityEditor: function() {
        var me = this;

        var isShowEditor = this.isShowEditor();
        var parentId = OPF.getQueryParam("parentId");
        if (OPF.isNotEmpty(parentId) && Ext.isNumeric((parentId))) {
            var model = Ext.create(this.model);
            Ext.each(model.associations.items, function(association) {
                if (association.name == 'parent' && association.type == 'belongsTo') {
                    var associationModel = Ext.create(association.model);
                    me.parentAssociation = association;
                    Ext.Ajax.request({
                        url: associationModel.self.restSuffixUrl + '/' + parentId,
                        method: 'GET',
                        success: function(response, action) {
                            var resp = Ext.decode(response.responseText);
                            me.parentModel = Ext.create(association.model, resp.data[0]);
                            me.formPanel.parentModel = me.parentModel;
                            me.refreshParentInfo();
                            if (isShowEditor) {
                                me.showEntityEditor();
                            }
                        },

                        failure: function(response) {
                            var responseStatus = Ext.decode(response.responseText);
                            var messages = [];
                            for (var i = 0; i < responseStatus.data.length; i++) {
                                var msg = responseStatus.data[i].msg;
                                messages.push(msg);
                            }
                            Ext.Msg.alert('Error', messages.join('<br/>'));
                        }
                    });
                }
            });
        } else if (isShowEditor) {
            this.showEntityEditor();
        }
    },

    showEntityEditor: function() {
        var model = Ext.create(this.model);
        var me = this;

        var entityId = OPF.getQueryParam("entityId");
        if (OPF.isNotEmpty(entityId) && Ext.isNumeric(entityId)) {
            this.formPanel.getEl().mask();
            Ext.Ajax.request({
                url: model.self.restSuffixUrl + '/' + entityId,
                method: 'GET',
                success: function(response, action) {
                    var vo = Ext.decode(response.responseText);
                    if (vo.success) {
                        var jsonData = vo.data[0];
                        var record = OPF.ModelHelper.createModelFromData(me.model, jsonData);
                        me.formPanel.getEl().unmask();
                        me.formPanel.showFormPanel(record);
                    } else {
                        Ext.Msg.alert('Error', vo.message);
                        me.formPanel.getEl().unmask();
                    }
                },

                failure: function(response) {
                    me.formPanel.getEl().unmask();
                    var responseStatus = Ext.decode(response.responseText);
                    var messages = [];
                    for (var i = 0; i < responseStatus.data.length; i++) {
                        var msg = responseStatus.data[i].msg;
                        messages.push(msg);
                    }
                    Ext.Msg.alert('Error', messages.join('<br/>'));
                }
            });
        } else {
            this.formPanel.showFormPanel();
        }
    },

    refreshParentInfo: function() {
        if (OPF.isNotEmpty(this.parentModel) && OPF.isNotEmpty(this.parentAssociation)) {
            var parentName = OPF.getQueryParam('parentName');
            var parentUrl = OPF.getQueryParam('link');
            if (OPF.isNotBlank(parentName) && OPF.isNotBlank(parentUrl)) {
                parentUrl = decodeURIComponent(parentUrl);
                var parentInfoHtml = '<div class="return-parent">';
                if (OPF.isEmpty(this.parentModel.self.rowTemplate)) {
                    parentInfoHtml += 'Return to ' + this.parentAssociation.displayName + ': ' + decodeURIComponent(parentName);
                } else {
                    parentInfoHtml += this.parentModel.self.rowTemplate;
                }

                parentInfoHtml += '<a class="parent-page-url" href="' + parentUrl + '"><span></span></a></div>';

                var upperContainer = this.up();
                var model = Ext.create(this.model);
                var selector = '[entityLookup=' + model.self.lookup + ']';
                var titleComponent = upperContainer.down(selector);
                titleComponent.setRightPlaceholder(parentInfoHtml, this.parentModel.data);
            }
        }
    }

/*
    refreshParentInfo: function() {
        if (OPF.isNotEmpty(this.parentModel) && OPF.isNotEmpty(this.parentAssociation)) {
            var parentName = OPF.getQueryParam('parentName');
            var parentUrl = OPF.getQueryParam('link');
            if (OPF.isNotBlank(parentName) && OPF.isNotBlank(parentUrl)) {
                parentUrl = decodeURIComponent(parentUrl);
                var parentInfoHtml = '<a class="parent-page-url" href="' + parentUrl + '">';
                parentInfoHtml += 'Return to ' + this.parentAssociation.displayName + ': ' + decodeURIComponent(parentName);
                parentInfoHtml += '</a>';
                this.parentLinksContainer.update(parentInfoHtml);
            }
        }
    }

*/
});