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

package net.firejack.platform.service.directory.broker.user;

import net.firejack.platform.api.directory.domain.User;
import net.firejack.platform.core.broker.ListBroker;
import net.firejack.platform.core.domain.NamedValues;
import net.firejack.platform.core.exception.BusinessFunctionException;
import net.firejack.platform.core.model.user.UserModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.store.user.IUserStore;
import net.firejack.platform.core.utils.Paging;
import net.firejack.platform.core.utils.SearchQuery;
import net.firejack.platform.core.utils.SortField;
import net.firejack.platform.utils.WebUtils;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Created by Open Flame Platform
 * Date: Mon Sep 30 11:30:31 EDT 2013
 */

/**
	*This action allows an authorized client to search for existing Users 
	*by providing search query parameters and sort orders. Required 
	*parameters and their descriptions are provided in the parameters list 
	*below. 	
*/

@TrackDetails
@Component("advancedSearchUserBroker")
public class AdvancedSearchUserBroker extends ListBroker<UserModel, User, NamedValues> {

    @Autowired
	private IUserStore store;

	@Override
	protected List<UserModel> getModelList(ServiceRequest<NamedValues> request) throws BusinessFunctionException {
		String queryParameters = (String) request.getData().get("queryParameters");
		Integer offset = (Integer) request.getData().get("offset");
		Integer limit = (Integer) request.getData().get("limit");
		String sortOrders = (String) request.getData().get("sortOrders");

        List<List<SearchQuery>> searchQueries;
        List<SortField> sortFields;
        try {
            searchQueries = WebUtils.deserializeJSON(queryParameters, List.class, List.class, SearchQuery.class);
            sortFields = WebUtils.deserializeJSON(sortOrders, List.class, SortField.class);
        } catch (IOException e) {
            throw new BusinessFunctionException("Could not deserialize query or sort parameters.\n" + e.getMessage(), e);
        }

        List<UserModel> models = null;
        Integer total = store.advancedSearchCount(searchQueries);
        if (total > 0) {
           models = store.advancedSearch(searchQueries, new Paging(offset, limit, sortFields));
        }

        return models;
	}
}