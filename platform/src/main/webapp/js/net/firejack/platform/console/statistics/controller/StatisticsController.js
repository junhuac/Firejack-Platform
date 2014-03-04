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

Ext.define('OPF.console.statistics.controller.Statistics', {
    extend: 'Ext.app.Controller',

    views: ['Statistics', 'SystemMetrics', 'LogEntryView', 'LogTransactionView'],

    stores: ['LogEntryStore', 'MetricsStore', 'LogTransactionStore'],

    models: ['LogEntry', 'MetricsEntry', 'LogTransactionModel'],

    init: function() {
        var logEntryStore = this.getLogEntryStoreStore();
        var logTransactionStore = this.getLogTransactionStoreStore();
        var metricsStore = this.getMetricsStoreStore();
        logEntryStore.addListener('beforeload',this.onBeforeLogEntryStoreLoad, this);
        logTransactionStore.addListener('beforeload',this.onBeforeLogTransactionStoreLoad, this);
        metricsStore.addListener('beforeload',this.onBeforeMetricsStoreLoad, this);

        this.control(
            {
                'entry-logs': {
                    activate: this.onTabActivate
                },
                'transaction-logs': {
                    activate: this.onTabActivate
                },
                'system-metrics': {
                    activate: this.onTabActivate
                },
                'entry-logs textfield[marked=true]': {
                    change: this.onRefreshLogEntriesFields,
                    keyup: this.onRefreshLogEntriesFields
                },
                'transaction-logs textfield[marked=true]': {
                    change: this.onRefreshLogTransactionsFields,
                    keyup: this.onRefreshLogTransactionsFields
                },
                'system-metrics textfield[marked=true]': {
                    change: this.onRefreshMetricsFields,
                    keyup: this.onRefreshMetricsFields
                },
                'entry-logs datefield': {
                    select: this.onRefreshLogEntriesFields
                },
                'entry-logs combo': {
                    select: this.onRefreshLogEntriesFields
                },
                'transaction-logs datefield': {
                    select: this.onRefreshLogTransactionsFields
                },
                'system-metrics datefield': {
                    select: this.onRefreshMetricsFields
                },
                'system-metrics combo': {
                    select: this.onRefreshMetricsFields
                },
                'entry-logs timefield': {
                    select: this.onRefreshLogEntriesFields
                },
                'transaction-logs timefield': {
                    select: this.onRefreshLogTransactionsFields
                },
                'system-metrics timefield': {
                    select: this.onRefreshMetricsFields
                },
                'entry-logs button[action=reset]': {
                    click: this.onResetLogEntriesFields
                },
                'transaction-logs button[action=reset]': {
                    click: this.onResetLogTransactionsFields
                },
                'system-metrics button[action=reset]': {
                    click: this.onResetMetricsFields
                },
                'transaction-logs button[action=group-by-time]': {
                    click: this.onLogTransactionsGroupBtnClick
                },
                'system-metrics button[action=group-by-time]': {
                    click: this.onMetricsGroupBtnClick
                }
            }
        )
    },

    onTabActivate: function(panel) {
        panel.grid.store.load({params:{start: 0, limit: panel.pageSize}});
    },

    onBeforeLogEntryStoreLoad: function(store, operation) {
        var logEntriesView = this.getStatisticsView('entry-logs');
        var selectedNodeLookup = this.getSelectedNodeLookup(logEntriesView);
        var url = '/search?term=' + logEntriesView.searchField.getRawValue();
        url += '&lookup=' + selectedNodeLookup;
        url += '&startDate=' + logEntriesView.startDateField.getRawValue();
        url += '&endDate=' + logEntriesView.endDateField.getRawValue();
        url += '&startTime=' + logEntriesView.startTimeField.getRawValue();
        url += '&endTime=' + logEntriesView.endTimeField.getRawValue();
        url += '&logEntryType=' + logEntriesView.logEntryType.getValue();

        store.proxy.url = store.registryNodeType.generateUrl(url);
    },

    onBeforeLogTransactionStoreLoad: function(store, operation) {
        var logTransactionView = this.getStatisticsView('transaction-logs');
        var selectedNodeLookup = this.getSelectedNodeLookup(logTransactionView);
        var url = '/search?term=' + logTransactionView.searchField.getRawValue();
        url += '&lookup=' + selectedNodeLookup;
        url += '&startDate=' + logTransactionView.startDateField.getRawValue();
        url += '&endDate=' + logTransactionView.endDateField.getRawValue();
        url += '&startTime=' + logTransactionView.startTimeField.getRawValue();
        url += '&endTime=' + logTransactionView.endTimeField.getRawValue();
        url += '&level=' + logTransactionView.transactionLogGroupLevel;

        store.proxy.url = store.registryNodeType.generateUrl(url);
    },

    onBeforeMetricsStoreLoad: function(store, operation) {
        var metricsView = this.getStatisticsView('system-metrics');
        var selectedNodeLookup = this.getSelectedNodeLookup(metricsView);
        var url = '/search?term=' + metricsView.searchField.getRawValue();
        url += '&lookup=' + selectedNodeLookup;
        url += '&startDate=' + metricsView.startDateField.getRawValue();
        url += '&endDate=' + metricsView.endDateField.getRawValue();
        url += '&startTime=' + metricsView.startTimeField.getRawValue();
        url += '&endTime=' + metricsView.endTimeField.getRawValue();
        url += '&level=' + metricsView.metricGroupLevel;
        url += '&logEntryType=' + metricsView.logEntryType.getValue();

        store.proxy.url = store.registryNodeType.generateUrl(url);
    },

    getSelectedNodeLookup: function(view) {
        var selectedNode = view.managerLayout.navigationPanel.getSelectedNode();
        var selectedNodeLookup = "";
        if (OPF.isNotEmpty(selectedNode) && OPF.isNotEmpty(selectedNode.data) &&
            OPF.isNotEmpty(selectedNode.data.lookup)) {
            selectedNodeLookup = selectedNode.data.lookup;
        }
        return selectedNodeLookup;
    },

    onRefreshLogEntriesFields: function() {
        var logEntriesView = this.getStatisticsView('entry-logs');
        logEntriesView.bottomToolbar.moveFirst();
    },

    onRefreshLogTransactionsFields: function() {
        var logTransactionsView = this.getStatisticsView('transaction-logs');
        logTransactionsView.bottomToolbar.moveFirst();
    },

    onRefreshMetricsFields: function() {
        var metricsView = this.getStatisticsView('system-metrics');
        metricsView.bottomToolbar.moveFirst();
    },

    onResetLogEntriesFields: function() {
        var logEntriesView = this.getStatisticsView('entry-logs');
        this.resetFields(logEntriesView);
    },

    onResetLogTransactionsFields: function() {
        var logTransactionsView = this.getStatisticsView('transaction-logs');
        this.resetFields(logTransactionsView);
    },

    onResetMetricsFields: function() {
        var metricsView = this.getStatisticsView('system-metrics');
        this.resetFields(metricsView);
    },

    onLogTransactionsGroupBtnClick: function(btn) {
        var logTransactionsView = this.getStatisticsView('transaction-logs');
        logTransactionsView.transactionLogGroupLevel = btn.groupLevel;
        logTransactionsView.bottomToolbar.moveFirst();
    },

    onMetricsGroupBtnClick: function(btn) {
        var metricsView = this.getStatisticsView('system-metrics');
        metricsView.metricGroupLevel = btn.groupLevel;
        metricsView.bottomToolbar.moveFirst();
    },

    resetFields: function(view) {
        view.searchField.setValue('');
        view.startDateField.setValue('');
        view.startTimeField.setValue('');
        view.endDateField.setValue('');
        view.endTimeField.setValue('');
        view.bottomToolbar.moveFirst();
    },

    getStatisticsView: function(xtype) {
        var transactionLogsViewArray = Ext.ComponentQuery.query(xtype);
        return transactionLogsViewArray[0];
    }

});