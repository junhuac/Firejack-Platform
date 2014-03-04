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

package net.firejack.platform.service.content.broker.resource.document;

import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.api.content.domain.DocumentResource;
import net.firejack.platform.core.model.registry.resource.DocumentResourceModel;
import net.firejack.platform.core.model.registry.resource.DocumentResourceVersionModel;
import net.firejack.platform.core.store.registry.resource.IResourceStore;
import net.firejack.platform.core.store.registry.resource.IResourceVersionStore;
import net.firejack.platform.core.utils.OpenFlame;
import net.firejack.platform.service.content.broker.resource.AbstractCreateNewResourceVersionsBroker;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;


@Component("createNewDocumentResourceVersionsBroker")
@TrackDetails
public class CreateNewDocumentResourceVersionsBroker
		extends AbstractCreateNewResourceVersionsBroker<DocumentResourceModel, DocumentResourceVersionModel, DocumentResource> {

	@Autowired
	@Qualifier("documentResourceStore")
	private IResourceStore<DocumentResourceModel> documentResourceStore;

	@Autowired
	@Qualifier("documentResourceVersionStore")
	private IResourceVersionStore<DocumentResourceVersionModel> documentResourceVersionStore;


	@Override
	public IResourceStore<DocumentResourceModel> getResourceStore() {
		return documentResourceStore;
	}

	@Override
	public IResourceVersionStore<DocumentResourceVersionModel> getResourceVersionStore() {
		return documentResourceVersionStore;
	}

	@Override
	public String getResourceName() {
		return "Document";
	}

	@Override
	protected void copyResourceFiles(DocumentResourceModel resource,
	                                 List<DocumentResourceVersionModel> oldResourceVersions,
	                                 List<DocumentResourceVersionModel> newResourceVersions) {
		if (!newResourceVersions.isEmpty()) {
			for (int i = 0, newResourceVersionsSize = newResourceVersions.size(); i < newResourceVersionsSize; i++) {
				DocumentResourceVersionModel oldResourceVersion = oldResourceVersions.get(i);
				DocumentResourceVersionModel newResourceVersion = newResourceVersions.get(i);
					String oldFilename = oldResourceVersion.getId() + "_" +
							oldResourceVersion.getVersion() + "_" +
							oldResourceVersion.getCulture().name();
					String newFilename = newResourceVersion.getId() + "_" +
							newResourceVersion.getVersion() + "_" +
							newResourceVersion.getCulture().name();

					InputStream stream = OPFEngine.FileStoreService.download(OpenFlame.FILESTORE_CONTENT, oldFilename,helper.getDocument(), String.valueOf(resource.getId()));
					OPFEngine.FileStoreService.upload(OpenFlame.FILESTORE_CONTENT,newFilename,stream, helper.getDocument(), String.valueOf(resource.getId()));
					IOUtils.closeQuietly(stream);
			}
		}
	}

}