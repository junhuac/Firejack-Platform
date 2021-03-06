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

package net.firejack.platform.web.security.model.attribute;

import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.api.directory.domain.User;
import net.firejack.platform.api.directory.domain.UserIdentity;
import net.firejack.platform.core.exception.BusinessFunctionException;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.utils.StringUtils;
import net.firejack.platform.web.cache.CacheManager;
import net.firejack.platform.web.security.model.OpenFlameSecurityConstants;
import net.firejack.platform.web.security.model.context.ContextManager;
import net.firejack.platform.web.security.model.context.OPFContext;
import net.firejack.platform.web.security.model.principal.SystemPrincipal;
import net.firejack.platform.web.security.model.principal.UserPrincipal;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class CachedContextFacade extends SecurityAttributeFacade {

    private static final Logger logger = Logger.getLogger(CachedContextFacade.class);

    protected final ConcurrentMap<String, OPFContext> cachedContexts =
            new ConcurrentHashMap<String, OPFContext>();
    protected ThreadLocal<String[]> sessionTokenHolder = new InheritableThreadLocal<String[]>();

    public void initSessionToken(String sessionToken, String ipAddress) {
        sessionTokenHolder.set(new String[]{sessionToken, ipAddress});
    }

    @Override
    public void invalidateBusinessContext() {
        synchronized (cachedContexts) {
            String[] currentSessionToken = sessionTokenHolder.get();
            if (currentSessionToken != null) {
                cleanContext(currentSessionToken[0]);
            }
        }
    }

    @Override
    public void setAttribute(OPFContext attribute) {
        synchronized (cachedContexts) {
            if (StringUtils.isNotBlank(attribute.getSessionToken())) {
                sessionTokenHolder.set(new String[]{attribute.getSessionToken(), attribute.getBrowserIpAddress()});
                cachedContexts.put(attribute.getSessionToken(), attribute);
            }
        }
    }

    @Override
    public OPFContext getAttribute() {
        OPFContext attribute;
        synchronized (cachedContexts) {
            String[] currentSessionToken = sessionTokenHolder.get();
            if (currentSessionToken == null || StringUtils.isBlank(currentSessionToken[0])) {
                attribute = null;
            } else {
                OPFContext context = cachedContexts.get(currentSessionToken[0]);
                //String browserIpAddress = context == null ? null : context.getBrowserIpAddress();
                if (isSessionTokenActive(currentSessionToken[0], currentSessionToken[1])) {
                    if (context == null) {
                        UserIdentity userIdentity = getUserIdentity(currentSessionToken[0]);
                        if (userIdentity == null) {
                            logger.error("Failed to load current user info.");
                        } else {
                            User user = new User();
                            user.setId(userIdentity.getId());
                            user.setUsername(userIdentity.getUserName());
                            user.setFirstName(userIdentity.getFirstName());
                            user.setLastName(userIdentity.getLastName());
                            //maybe need to also set list of roles
                            Set<String> systemLevelSessions = CacheManager.getInstance().getSystemLevelSessions();
                            UserPrincipal principal = systemLevelSessions != null &&
                                    systemLevelSessions.contains(currentSessionToken[0]) ?
                                    new SystemPrincipal(user) : new UserPrincipal(user);
                            context = OPFContext.initContext(principal, currentSessionToken[0]);
                            cachedContexts.put(currentSessionToken[0], context);
                        }
                    }
                    attribute = context;
                } else {
                    if (context != null) {
                        cleanContext(currentSessionToken[0]);
                    }
                    attribute = null;
                }
            }
        }
        return attribute;
    }

    public void releaseAttributeResources() {
        this.sessionTokenHolder.remove();
    }

    protected void cleanContext(String sessionToken) {
        OPFContext removedContext = cachedContexts.remove(sessionToken);
        Long userId = removedContext == null || removedContext.getPrincipal().isGuestPrincipal() ?
                null : removedContext.getPrincipal().getUserInfoProvider().getId();
        CacheManager cacheManager = CacheManager.getInstance();
        if (cacheManager.isLocal()) {
            cleanLocalCacheData(sessionToken, userId);
        }
    }

    protected boolean isSessionTokenActive(String sessionToken, String browserIpAddress) {
        return ContextManager.isSessionActive(sessionToken, browserIpAddress);
    }

    protected UserIdentity getUserIdentity(String sessionToken) {
        ServiceResponse<UserIdentity> response = OPFEngine.DirectoryService.getUserIdentityInfo(sessionToken);
        if (response == null) {
            throw new IllegalStateException("API Service response should not be null.");
        } else if (response.isSuccess()) {
            return response.getItem();
        } else {
            logger.error("Directory API Service call returned failure response. Reason: " + response.getMessage());
            throw new BusinessFunctionException(response.getMessage());
        }
    }

    protected void cleanLocalCacheData(String sessionToken, Long userId) {
        if (OpenFlameSecurityConstants.isClientContext()) {
            CacheManager.getInstance().invalidateLocalData(sessionToken, userId);
        }
    }

}