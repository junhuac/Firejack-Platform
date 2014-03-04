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

package net.firejack.platform.web.security.siteminder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;
import net.firejack.platform.api.ServiceProxyExecutor;
import net.firejack.platform.core.domain.SimpleIdentifier;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.utils.StringUtils;
import net.firejack.platform.provider.XMLProvider;
import net.firejack.platform.web.handler.Builder;
import net.firejack.platform.web.handler.ErrorHandler;
import net.firejack.platform.web.security.SecurityUtils;
import net.firejack.platform.web.security.model.OpenFlameSecurityConstants;
import net.firejack.platform.web.security.model.context.ContextLookupException;
import net.firejack.platform.web.security.model.context.OPFContext;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 *
 */
public class DefaultUserAccessControlHandler implements UserAccessControlHandler {

    protected static final int TIMEOUT = 60000;
    private static final Logger logger = Logger.getLogger(DefaultUserAccessControlHandler.class);

    private String handlerUrl;
    private String roleLookupHandlerUrl;

    public void setHandlerUrl(String handlerUrl) {
        this.handlerUrl = handlerUrl;
    }

    public void setRoleLookupHandlerUrl(String roleLookupHandlerUrl) {
        this.roleLookupHandlerUrl = roleLookupHandlerUrl;
    }

    @Override
    public boolean userIsActive(String standardId) {
        boolean active = true;
        if (StringUtils.isBlank(this.handlerUrl)) {
            logger.warn("SiteMinder user access control handler url is not set.");
        } else {
            if (StringUtils.isBlank(standardId)) {
                logger.warn("standardId parameter specified is blank.");
            } else {
                try {
                    Builder proxy = prepareBuilder(this.handlerUrl, standardId);
                    ServiceResponse<SimpleIdentifier> resp = doGet(proxy, ServiceResponse.class);
                    if (resp.isSuccess()) {
                        SimpleIdentifier<Boolean> item = resp.getItem();
                        if (item != null && item.getIdentifier() != null) {
                            active = item.getIdentifier();
                        }
                    } else {
                        logger.warn("Failed to retrieve user status. Reason: " + resp.getMessage());
                    }
                } catch (Throwable th) {
                    logger.warn(th.getMessage(), th);
                    // we do not want open-flame stop to work due client side application errors
                    active = true;
                }
            }
        }
        return active;
    }

    @Override
    public List<String> roleLookupList(String standardId) {
        List<String> result;
        if (StringUtils.isBlank(this.roleLookupHandlerUrl)) {
            logger.warn("SiteMinder user access control handler url is not set.");
            result = Collections.emptyList();
        } else {
            try {
                Builder proxy = prepareBuilder(this.roleLookupHandlerUrl, standardId);
                @SuppressWarnings("unchecked")
                ServiceResponse<SimpleIdentifier<String>> resp = doGet(proxy, ServiceResponse.class);
                if (resp.isSuccess()) {
                    List<SimpleIdentifier<String>> data = resp.getData();
                    result = new ArrayList<String>();
                    if (data != null) {
                        for (SimpleIdentifier<String> identifier : data) {
                            result.add(identifier.getIdentifier());
                        }
                    }
                } else {
                    logger.warn("Failed to retrieve user role. Reason: " + resp.getMessage());
                    result = Collections.emptyList();
                }
            } catch (Throwable th) {
                logger.warn(th.getMessage(), th);
                result = Collections.emptyList();
            }
        }
        return result;
    }

    protected <T> T doGet(final Builder builder, final Class<T> clazz) {
        return new ServiceProxyExecutor<T>() {
            @Override
            public T doRequest(Builder builder) {
                return builder.get(clazz);
            }
        }.request(builder);
    }

    protected Builder prepareBuilder(String url, String standardId) throws Throwable {
        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(JacksonJsonProvider.class);
        config.getClasses().add(XMLProvider.class);
        config.getSingletons().add(new SingletonTypeInjectableProvider<Context, Class[]>(
                Class[].class, new Class[]{SimpleIdentifier.class}) {});
        WebResource webResource = Client.create(config).resource(
                url + (StringUtils.isBlank(standardId) ? "" : "?standardId=" + standardId));
        webResource.setProperty(ClientConfig.PROPERTY_CONNECT_TIMEOUT, TIMEOUT);
        webResource.setProperty(ClientConfig.PROPERTY_READ_TIMEOUT, TIMEOUT);
        WebResource.Builder builder = webResource
                .accept(MediaType.APPLICATION_XML_TYPE).type(MediaType.APPLICATION_XML_TYPE);

        if (OPFContext.isInitialized()) {
            try {
                OPFContext context = OPFContext.getContext();
                String sessionToken = context.getSessionToken();
                if (sessionToken != null) {
                    Cookie cookie = new Cookie(OpenFlameSecurityConstants.AUTHENTICATION_TOKEN_ATTRIBUTE, sessionToken);
                    builder.cookie(cookie);
                }
                builder.header(OpenFlameSecurityConstants.MARKER_HEADER, this.getClass().getName());
                if (context.getBrowserIpAddress() != null) {
                    String key = sessionToken.substring(4, 12);
                    String clientIp = SecurityUtils.encryptData(context.getBrowserIpAddress(), key);
                    builder.header(OpenFlameSecurityConstants.CLIENT_INFO_HEADER, clientIp);
                }
            } catch (ContextLookupException e) {
                logger.error(e.getMessage());
            }
        }

        return ErrorHandler.getProxy(builder);
    }
}