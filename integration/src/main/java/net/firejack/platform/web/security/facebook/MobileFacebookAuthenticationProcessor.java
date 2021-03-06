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

package net.firejack.platform.web.security.facebook;

import com.google.code.facebookapi.ProfileField;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.api.authority.domain.AuthenticationToken;
import net.firejack.platform.core.exception.BusinessFunctionException;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.utils.StringUtils;
import net.firejack.platform.utils.WebUtils;
import net.firejack.platform.web.security.filter.IAuthenticationProcessor;
import net.firejack.platform.web.security.model.OpenFlameSecurityConstants;
import net.firejack.platform.web.security.model.context.OPFContext;
import net.firejack.platform.web.security.model.principal.OpenFlamePrincipal;
import net.firejack.platform.web.security.model.principal.UserPrincipal;
import org.apache.log4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MobileFacebookAuthenticationProcessor implements IAuthenticationProcessor {

    private static final String FB_PARAM_ACCESS_TOKEN = "access_token";
    private static final Logger logger = Logger.getLogger(MobileFacebookAuthenticationProcessor.class);
    private String mobileFacebookEntryPointUrl;

    @Override
    public void initialize(FilterConfig config) throws ServletException {

    }

    @Override
    public boolean isAuthenticationCase(HttpServletRequest request) {
        String requestPath = WebUtils.getRequestPath(request);
        String accessTokenParameter = request.getParameter(FB_PARAM_ACCESS_TOKEN);
        boolean result = false;
        if (requestPath.equalsIgnoreCase(getMobileFacebookEntryPointUrl())) {
            if (StringUtils.isBlank(accessTokenParameter)) {
                logger.warn("No access token information were specified in mobile facebook authentication url.");
            } else {
                result = true;
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void processAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        String accessTokenParameter = request.getParameter(FB_PARAM_ACCESS_TOKEN);
        if (StringUtils.isBlank(accessTokenParameter)) {
            throw new BusinessFunctionException(FB_PARAM_ACCESS_TOKEN + " should not be blank.");
        } else {
            FacebookClient facebookClient = new DefaultFacebookClient(accessTokenParameter);
            ServiceResponse serviceResponse;
            try {
                com.restfb.types.User user = facebookClient.fetchObject("me", com.restfb.types.User.class);
                if (user == null) {
                    serviceResponse = new ServiceResponse("Failed to retrieve user information from fcebook", false);
                } else {
                    String browserIpAddress = request.getRemoteAddr();
                    AuthenticationToken opfToken = getOpenFlameToken(user, browserIpAddress);
                    if (opfToken == null) {
                        serviceResponse = new ServiceResponse("OpenFlame returned token is null.", false);
                    } else {
                        OpenFlamePrincipal principal = new UserPrincipal(opfToken.getUser());
                        OPFContext.initContext(principal, opfToken.getToken());
                        serviceResponse = new ServiceResponse(opfToken, "token successfully obtained.", true);
                    }
                }
            } catch (FacebookException e) {
                logger.error(e.getMessage(), e);
                serviceResponse = new ServiceResponse(e.getMessage(), false);
            }
            processMobilePhoneResponse(serviceResponse, request, response);
        }
    }

    @Override
    public void processUnAuthentication(HttpServletRequest request, HttpServletResponse response) {
        //nothing to do here yet
    }

    @Override
    public void release() {
    }

    public String getMobileFacebookEntryPointUrl() {
        return mobileFacebookEntryPointUrl;
    }

    public void setMobileFacebookEntryPointUrl(String mobileFacebookEntryPointUrl) {
        this.mobileFacebookEntryPointUrl = mobileFacebookEntryPointUrl;
    }

    private AuthenticationToken getOpenFlameToken(com.restfb.types.User user, String browserIpAddress) {
        Map<String, String> mappedAttributes = new HashMap<String, String>();
        mappedAttributes.put(ProfileField.NAME.fieldName(), user.getUsername());
        mappedAttributes.put(ProfileField.FIRST_NAME.fieldName(), user.getFirstName());
        mappedAttributes.put(ProfileField.LAST_NAME.fieldName(), user.getLastName());
        ServiceResponse<AuthenticationToken> response =
                OPFEngine.AuthorityService.processFacebookIdSignIn(
                        Long.parseLong(user.getId()), mappedAttributes, browserIpAddress);
        AuthenticationToken token;
        if (response == null) {
            throw new IllegalStateException("API Service response should not be null.");
        } else if (response.isSuccess()) {
            token = response.getItem();
        } else {
            logger.error("API Service response has failure status. Reason: " + response.getMessage());
            token = null;
        }
        return token;
    }

    private void processMobilePhoneResponse(
            Object respObject, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String requestType = request.getContentType() == null ?
                request.getHeader("accept") : request.getContentType();
        if (MediaType.APPLICATION_XML.equalsIgnoreCase(requestType)) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance();
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                ServletOutputStream out = response.getOutputStream();
                marshaller.marshal(respObject, out);
                out.close();
            } catch (JAXBException e) {
                logger.error(e.getMessage(), e);
                OpenFlameSecurityConstants.printXmlErrorToResponse(
                        response, OpenFlameSecurityConstants.API_SECURITY_ERROR_RESPONSE.format(
                        new String[]{e.getMessage()}));

            }
        } else {
            String jsonData;
            try {
                jsonData = WebUtils.serializeObjectToJSON(respObject);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                jsonData = "{ success: false; message: '" + e.getMessage() + "' }";
            }
            ServletOutputStream out = response.getOutputStream();
            out.println(jsonData);
            out.close();
        }
    }
}