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

package net.firejack.platform.core.store.registry.resource;

import net.firejack.platform.api.content.model.ResourceStatus;
import net.firejack.platform.core.model.registry.HTTPMethod;
import net.firejack.platform.core.model.registry.IAllowCreateAutoDescription;
import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.RegistryNodeType;
import net.firejack.platform.core.model.registry.domain.ActionModel;
import net.firejack.platform.core.model.registry.resource.*;
import net.firejack.platform.core.utils.MessageResolver;
import net.firejack.platform.core.utils.StringUtils;
import net.firejack.platform.generate.VelocityGenerator;
import net.firejack.platform.model.service.DocumentationLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HtmlResourceStore extends ResourceStore<HtmlResourceModel> implements IHtmlResourceStore {

    public static final String DESCRIPTION = "Description";
    @Autowired
	@Qualifier("documentationLinkService")
	private DocumentationLinkService documentationLinkService;
	@Autowired
	protected VelocityGenerator generator;

	@Autowired
	@Qualifier("collectionStore")
	private ICollectionStore collectionStore;

	/***/
	@PostConstruct
	public void init() {
		setClazz(HtmlResourceModel.class);
	}

	@Override
	@Transactional
	public void createDescription(ActionModel action, CollectionModel collection) {
		ResourceModel resource = saveHtmlResource(collection.getParent(), "Description", action.getDescription());
		collectionStore.associateCollectionWithReference(collection, resource);
	}

	@Override
	@Transactional
	public void createAutoDescription(RegistryNodeModel registryNode) {
		String description = registryNode.getDescription();
		if (registryNode instanceof IAllowCreateAutoDescription) {
			CollectionModel collection = collectionStore.findOrCreateCollection(registryNode);
			if (StringUtils.isNotBlank(description)) {
				ResourceModel resource = saveHtmlResource(collection.getParent(), DESCRIPTION, description);
				collectionStore.associateCollectionWithReference(collection, resource);
			}
		}
	}

	@Override
	@Transactional
	public void createRestExample(ActionModel action, CollectionModel collection) {
		String restUrl = documentationLinkService.generateRestUrl(action);
		Map model = new HashMap();

		String request = "";
		if (HTTPMethod.POST.equals(action.getMethod()) || HTTPMethod.PUT.equals(action.getMethod())) {
			request = documentationLinkService.generateRestRequestExample(action);
		}
		String response = documentationLinkService.generateRestResponseExample(action);

		model.put("method", action.getMethod());
		model.put("date", new Date());
		model.put("length", response.length());
		model.put("rest", restUrl);
		model.put("request", request);
		model.put("response", response);

		String title = MessageResolver.messageFormatting("rest.example.title", Locale.ENGLISH);
		String example = generator.compose("templates/example/rest.vsl", model);

		ResourceModel resource = saveHtmlResource(collection.getParent(), title, example);
		collectionStore.associateCollectionWithReference(collection, resource);
	}

	@Override
	@Transactional
	public void createSoapExample(ActionModel action, CollectionModel collection) {
		Map model = new HashMap();

		String request = documentationLinkService.generateSoapRequestExample(action);
		String response = documentationLinkService.generateSoapResponseExample(action);

		model.put("method", action.getMethod());
		model.put("date", new Date());
		model.put("name", action.getSoapMethod());
		model.put("url", action.getSoapUrlPath());
		model.put("request", request);
		model.put("request_length", request.length());
		model.put("response", response);
		model.put("response_length", response.length());
		model.put("host", "${host}");
		model.put("port", "${port}");
		model.put("context", "${context}");
		model.put("service", findContext(action));
		model.put("username", "${username}");
		model.put("password", "${password}");

		String title = MessageResolver.messageFormatting("soap.example.title", Locale.ENGLISH);
		String example = generator.compose("templates/example/soap.vsl", model);

		example = HtmlUtils.htmlEscape(example).replace("~#", "<").replace("#~", ">");

		ResourceModel resource = saveHtmlResource(collection.getParent(), title, example);
		collectionStore.associateCollectionWithReference(collection, resource);
	}

	private String findContext(RegistryNodeModel model) {
		if (model != null) {
			if (model.getType()==RegistryNodeType.DOMAIN || model.getType()==RegistryNodeType.PACKAGE) {
				return StringUtils.normalize(model.getName()) + "."+findContext(model.getParent());
			} else if(model.getType()==RegistryNodeType.ROOT_DOMAIN) {
				return model.getName();
			} else {
				return findContext(model.getParent());
			}
		}
		return "";
	}

	private HtmlResourceModel saveHtmlResource(RegistryNodeModel registryNode, String name, String html) {
		String lookup = registryNode.getLookup() + "." + StringUtils.normalize(name);
		HtmlResourceModel resource = findByLookup(lookup);
		if (resource == null) {
			resource = new HtmlResourceModel();
			resource.setName(name);
			resource.setParent(registryNode);
			resource.setLastVersion(1);
			resource.setPublishedVersion(1);

			HtmlResourceVersionModel resourceVersion = new HtmlResourceVersionModel();
			resourceVersion.setHtml(html);
			resource.setResourceVersion(resourceVersion);

			//TODO may be need to create a rest example for all cultures
			resourceVersion.setResource(resource);
			resourceVersion.setCulture(Cultures.AMERICAN);
			resourceVersion.setStatus(ResourceStatus.PUBLISHED);
			resourceVersion.setUpdated(new Date());
			resourceVersion.setVersion(resource.getLastVersion());
			save(resource);
		} else {
			HtmlResourceVersionModel resourceVersion = (HtmlResourceVersionModel)resourceVersionStore.findLastVersionByResourceIdCulture(resource.getId(), Cultures.AMERICAN);
			resourceVersion.setHtml(html);
		}

		return resource;
	}

}
