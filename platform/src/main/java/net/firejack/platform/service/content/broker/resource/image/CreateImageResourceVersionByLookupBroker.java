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

package net.firejack.platform.service.content.broker.resource.image;

import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.api.content.domain.ImageResourceVersion;
import net.firejack.platform.core.broker.ServiceBroker;
import net.firejack.platform.core.config.meta.utils.DiffUtils;
import net.firejack.platform.core.domain.NamedValues;
import net.firejack.platform.core.exception.BusinessFunctionException;
import net.firejack.platform.core.model.registry.RegistryNodeModel;
import net.firejack.platform.core.model.registry.resource.FolderModel;
import net.firejack.platform.core.model.registry.resource.ImageResourceModel;
import net.firejack.platform.core.model.registry.resource.ImageResourceVersionModel;
import net.firejack.platform.core.request.ServiceRequest;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.store.registry.IRegistryNodeStore;
import net.firejack.platform.core.store.registry.resource.IFolderStore;
import net.firejack.platform.core.store.registry.resource.IResourceVersionStore;
import net.firejack.platform.core.store.registry.resource.ImageResourceStore;
import net.firejack.platform.core.utils.ImageUtils;
import net.firejack.platform.core.utils.OpenFlame;
import net.firejack.platform.core.utils.StringUtils;
import net.firejack.platform.core.validation.ValidationMessage;
import net.firejack.platform.core.validation.exception.RuleValidationException;
import net.firejack.platform.model.helper.FileHelper;
import net.firejack.platform.web.statistics.annotation.TrackDetails;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Component("createImageResourceVersionByLookupBroker")
@TrackDetails
public class CreateImageResourceVersionByLookupBroker
        extends ServiceBroker<ServiceRequest<NamedValues>, ServiceResponse<ImageResourceVersion>> {

    @Autowired
    @Qualifier("registryNodeStore")
    private IRegistryNodeStore<RegistryNodeModel> registryNodeStore;

    @Autowired
    @Qualifier("imageResourceStore")
    public ImageResourceStore imageResourceStore;

    @Autowired
    @Qualifier("imageResourceVersionStore")
    private IResourceVersionStore<ImageResourceVersionModel> resourceVersionStore;

    @Autowired
    @Qualifier("folderStore")
    private IFolderStore folderStore;

    @Autowired
    protected FileHelper helper;

    @Override
    protected List<ValidationMessage> specificArgumentsValidation(ServiceRequest<NamedValues> request) throws RuleValidationException {
        List<ValidationMessage> validationMessages = new ArrayList<ValidationMessage>();
        String resourceLookup = (String) request.getData().get("resourceLookup");
        RegistryNodeModel registryNodeModel = registryNodeStore.findByLookup(resourceLookup);
        if (registryNodeModel != null) {
            ValidationMessage validationMessage = new ValidationMessage(
                    null, "resource.already.exist", resourceLookup);
            validationMessages.add(validationMessage);
        }
        return validationMessages;
    }


    @Override
    protected ServiceResponse<ImageResourceVersion> perform(ServiceRequest<NamedValues> request) throws BusinessFunctionException {
        String resourceLookup = (String) request.getData().get("resourceLookup");
        ImageResourceVersion imageResourceVersion = (ImageResourceVersion) request.getData().get("imageResourceVersion");

        try {
            String parentLookup = DiffUtils.extractPathFromLookup(resourceLookup);
//            RegistryNodeModel parentModel = registryNodeStore.findByLookup(parentLookup);
            RegistryNodeModel parentModel = findOrCreateFolder(parentLookup);
            if (parentModel != null) {
                String name = DiffUtils.humanNameFromLookup(resourceLookup);

                ImageResourceModel imageResourceModel = new ImageResourceModel();
                imageResourceModel.setName(name);
                imageResourceModel.setParent(parentModel);

                ImageResourceVersionModel imageResourceVersionModel = factory.convertFrom(ImageResourceVersionModel.class, imageResourceVersion);
                imageResourceVersionModel.setResource(imageResourceModel);
                imageResourceVersionModel.setVersion(1);

                imageResourceModel.setResourceVersion(imageResourceVersionModel);
                imageResourceModel.setSelectedVersion(1);
                imageResourceModel.setLastVersion(1);

                imageResourceStore.save(imageResourceModel);

                save(imageResourceModel);

                imageResourceVersion = factory.convertTo(ImageResourceVersion.class, imageResourceVersionModel);
                return new ServiceResponse<ImageResourceVersion>(imageResourceVersion, "Image resource saved successfully.", true);
            } else {
                return new ServiceResponse<ImageResourceVersion>("Can't find parent for created resource.", false);
            }
        } catch (Exception e) {
            logger.error("error saving image resource", e);
            return new ServiceResponse<ImageResourceVersion>("Error saving image resource!", false);
        }
    }

    protected void save(ImageResourceModel imageResourceModel) throws Exception {

        String temporaryUploadedFileName = imageResourceModel.getResourceVersion().getTemporaryFilename();
	    InputStream stream = OPFEngine.FileStoreService.download(OpenFlame.FILESTORE_BASE, temporaryUploadedFileName, helper.getTemp());

	    if (stream != null) {
            try {
                byte[] bytes = IOUtils.toByteArray(stream);
		        IOUtils.closeQuietly(stream);

                ImageResourceVersionModel imageResourceVersion = imageResourceModel.getResourceVersion();
                String imageResourceVersionFilename = imageResourceVersion.getId() + "_" +
                                                      imageResourceVersion.getVersion() + "_" +
                                                      imageResourceVersion.getCulture().name();
                OPFEngine.FileStoreService.upload(OpenFlame.FILESTORE_CONTENT,imageResourceVersionFilename,new ByteArrayInputStream(bytes), helper.getImage(), String.valueOf(imageResourceModel.getId()));

                Integer[] size = ImageUtils.getImageSize(bytes);
                imageResourceVersion.setWidth(size[0]);
                imageResourceVersion.setHeight(size[1]);
                resourceVersionStore.saveOrUpdate(imageResourceVersion);
                imageResourceVersion.setTemporaryFilename(null);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private RegistryNodeModel findOrCreateFolder(String lookup) {
        RegistryNodeModel parent = registryNodeStore.findByLookup(lookup);
        if (parent == null) {
            String path = DiffUtils.extractPathFromLookup(lookup);
            RegistryNodeModel grandParent = findOrCreateFolder(path);
            parent = new FolderModel();
            String name = DiffUtils.humanNameFromLookup(lookup);
            parent.setName(StringUtils.normalize(name));
            parent.setParent(grandParent);
            folderStore.save((FolderModel) parent);
        }
        return parent;
    }
}
