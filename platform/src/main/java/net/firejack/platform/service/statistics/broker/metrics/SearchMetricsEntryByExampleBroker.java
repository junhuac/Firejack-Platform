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

package net.firejack.platform.service.statistics.broker.metrics;

import net.firejack.platform.api.statistics.domain.MetricsEntry;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.model.registry.statistics.MetricsEntryModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.statistics.IMetricsEntryStore;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@TrackDetails
@Component("searchMetricsEntryByExampleBrokerEx")
public class SearchMetricsEntryByExampleBroker extends ServiceBroker
        <ServiceRequest<MetricsEntry>, ServiceResponse<MetricsEntry>> {

    @Autowired
    private IMetricsEntryStore metricsEntryStore;

    @Override
    protected ServiceResponse<MetricsEntry> perform(ServiceRequest<MetricsEntry> request)
		    throws Exception {
        MetricsEntry example = request.getData();
        ServiceResponse<MetricsEntry> response;
        if (example == null) {
            response = new ServiceResponse<MetricsEntry>("Metrics entry example is null.", false);
        } else {
            try {
                MetricsEntryModel modelExample = factory.convertFrom(MetricsEntryModel.class, example);
                MetricsEntryModel foundMetrics = metricsEntryStore.findByExample(modelExample);
                MetricsEntry metricsEntry = factory.convertTo(MetricsEntry.class, foundMetrics);
                List<MetricsEntry> result = new ArrayList<MetricsEntry>();
                result.add(metricsEntry);
                response = new ServiceResponse<MetricsEntry>(result);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                response = new ServiceResponse<MetricsEntry>(e.getMessage(), false);
            }
        }
        return response;
    }

}