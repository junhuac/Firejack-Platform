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

package net.firejack.platform.web.security.permission;

import net.firejack.platform.api.authority.domain.UserPermission;

import java.security.Principal;
import java.util.List;
import java.util.Map;


public interface IPermissionContainer {

    /**
     * Loads all granted permissions for the provided user.
     *
     * @param p Principal that used for granted permissions load
     * @return List<String> a list of granted permissions for this user
     */
    List<UserPermission> loadGrantedActions(Principal p);

    /**
     * Loads all denied permissions (explicitly denied) for the provided user.
     *
     * @param p Principal that used for denied permissions load
     * @return List<String> a list of explicitly denied permissions for this user
     */
    List<UserPermission> loadDeniedActions(Principal p);

    /**
     * Load user permissions mapped to secured record Ids.
     *
     * @param p Principal that used for permissions load
     * @param securedRecordId securedRecordId
     * @return user permissions mapped to secured records
     */
    List<UserPermission> loadUserPermissionsBySecuredRecords(Principal p, Long securedRecordId);

    /**
     * Load user permissions mapped to secured record Ids.
     *
     * @param p Principal that used for permissions load
     * @param packageLookup packageLookup
     * @return user permissions mapped to secured records
     */
    List<UserPermission> loadPackageLevelUserPermissions(Principal p, String packageLookup);

    /**
     * Load context permissions by secured records map for specified user id
     * @param userId user id
     * @return context permissions by secured records map.
     */
    Map<Long, List<UserPermission>> loadSecuredRecordContextPermissions(Long userId);

}
