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
    'OPF.prometheus.component.manager.SearchManagerComponent',
    'OPF.prometheus.component.securitycontroller.SecurityControllerComponent'
]);

Ext.define('OPF.prometheus.component.manager.AbstractGridComponent', {
    extend: 'Ext.panel.Panel',

    border: false,

    model: null,

    configs: null,

    constructor: function(cfg) {
        cfg = cfg || {};
        cfg.configs = cfg.configs || {};

        this.callParent(arguments);
    },

    initComponent: function() {
        var me = this;

        me.addEvents(
            'storebeforeload',
            'storebeforesync',
            'storeload',
            'storeupdate'
        );

        this.callParent(arguments);
    },

    getModel: function() {
        return Ext.create(this.model);
    },

    getStore: function(options) {
        var me = this;

        return Ext.create('Ext.data.Store', Ext.apply({
            autoLoad: true,
            pageSize: 10,
            model: this.modelInstance.self,
            remoteSort: true,
            proxy: {
                type: 'rest',
                url: this.modelInstance.self.restSuffixUrl,
                reader: {
                    root: 'data',
                    totalProperty: 'total',
                    messageProperty: 'message',
                    idProperty: this.modelInstance.idProperty
                },
                writer: {
                    root: 'data'
                },
                simpleSortMode: true,
                startParam: 'offset',
                limitParam: 'limit',
                sortParam: 'sortColumn',
                directionParam: 'sortDirection'
            },
            listeners: {
                beforeload: function(store, operation) {
                    me.onStoreBeforeLoad(store, operation);
                },
                beforesync: function(options) {
                    me.onStoreBeforeSync(this, options);
                },
                load: function(store, records) {
                    me.onStoreLoad(store, records);
                },
                'update': function(store, record, operation, eOpts) {
                    me.onStoreUpdate(store, record, operation);
                }
            }
        }, options));
    },

    onStoreBeforeLoad: function(store, operation) {
        if (this.fireEvent('storebeforeload', this, store, operation) !== true) {
            store.proxy.url = store.model.restSuffixUrl;
        }
    },

    onStoreBeforeSync: function(store, operation) {
        if (this.fireEvent('storebeforesync', this, store, operation) !== true) {
            store.proxy.url = store.model.restSuffixUrl;
        }
    },

    onStoreLoad: function(store, records) {
        this.fireEvent('storeload', this, store, records);
    },

    onStoreUpdate: function(store, record, operation) {
        this.fireEvent('storeupdate', this, store, record, operation);
    },

    getColumnsByModel: function () {
        var me = this;

        var columns = [];
        Ext.each(this.modelInstance.fields.items, function (field) {
            if (field.fieldType) {
                var headerName = field.displayName || field.name;
                var column = {
                    id: field.name,
                    text: headerName,
                    dataIndex: field.name,
                    //                    minWidth: OPF.calculateColumnWidth(headerName),
                    //                    flex: OPF.calculateColumnWidth(headerName),
                    hidden: field.hidden,
                    minWidth: me.calculateColumnWidth(field.fieldType),
                    flex: me.calculateColumnWidth(field.fieldType),
                    sortable: true,
                    fieldType: field.fieldType,
                    isAssociation: false,
                    renderer: 'htmlEncode'
                };
                switch (field.fieldType) {
                    case 'IMAGE_FILE':
                        column.renderer = function (value) {
                            var result = '';
                            if (OPF.isNotBlank(value)) {
                                result = '<img src="' + OPF.Cfg.fullUrl('images/icons/16/image_16.png') + '"/>';
                            }
                            return result;
                        };
                        break;
                    case 'CURRENCY':
                        column.renderer = function (value) {
                            var cfg = {
                                allowDecimals: true,
                                alwaysDisplayDecimals: true,
                                currencySymbol: '$',
                                useThousandSeparator: true,
                                thousandSeparator: ',',
                                decimalPrecision: 2,
                                decimalSeparator: '.'
                            };
                            return OPF.core.component.form.Number.formattedValue(value, cfg);
                        };
                        break;
                }
                var columnConfig = field.gridConfig || {};
                column = Ext.apply(column, columnConfig);
                column = me.applyColumnConfig(column);
                columns.push(column);
            }
        });

        Ext.each(this.modelInstance.associations.items, function (association) {
            var component;
            if (association.type == 'belongsTo') {
                var associationModel = Ext.create(association.model);
                var headerName = association.displayName || OPF.getSimpleClassName(association.model);
                var column = {
                    id: association.name + '_' + associationModel.displayProperty,
                    text: headerName,
                    dataIndex: association.name + '.' + associationModel.displayProperty,
                    //                    minWidth: OPF.calculateColumnWidth(headerName),
                    //                    flex: OPF.calculateColumnWidth(headerName),

                    minWidth: me.calculateColumnWidth('NAME'),
                    flex: me.calculateColumnWidth('NAME'),
                    isAssociation: false,
                    sortable: false,
                    renderer: function (value, meta, record) {
                        var associationInstance = record[association.name + 'BelongsToInstance'];
                        if (associationInstance) {
                            var associationData = associationInstance.data;
                            value = associationData[associationModel.displayProperty];
                        }
                        return value;
                    }
                };
                var columnConfig = association.gridConfig || {};
                column = Ext.apply(column, columnConfig);
                column = me.applyColumnConfig(column);
                columns.push(column);
            }
        });
        return columns;
    },

    applyColumnConfig: function(column) {
        return column;
    },

    calculateColumnWidth: function(fieldType) {
        var width;
        switch(fieldType) {
            case 'NUMERIC_ID':
            case 'UNIQUE_ID':
                width = 80;
                break;
            case 'INTEGER_NUMBER':
            case 'LARGE_NUMBER':
                width = 80;
                break;
            case 'DECIMAL_NUMBER':
                width = 100;
                break;
            case 'DATE':
                width = 100;
                break;
            case 'TIME':
                width = 100;
                break;
            case 'EVENT_TIME':
            case 'CREATION_TIME':
            case 'UPDATE_TIME':
                width = 120;
                break;
            case 'PASSWORD':
                width = 100;
                break;
            case 'NAME':
                width = 200;
                break;
            case 'DESCRIPTION':
            case 'MEDIUM_TEXT':
            case 'LONG_TEXT':
            case 'UNLIMITED_TEXT':
            case 'RICH_TEXT':
                width = 300;
                break;
            case 'CURRENCY':
                width = 100;
                break;
            case 'PHONE_NUMBER':
            case 'SSN':
                width = 100;
                break;
            case 'FLAG':
                width = 50;
                break;
            case 'IMAGE_FILE':
                width = 50;
                break;
            default:
                width = 100;
                break;
        }
        return width;
    }

});