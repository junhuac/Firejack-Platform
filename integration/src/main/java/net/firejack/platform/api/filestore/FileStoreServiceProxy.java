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

package net.firejack.platform.api.filestore;

import com.sun.jersey.core.util.Base64;
import net.firejack.platform.api.AbstractServiceProxy;
import net.firejack.platform.api.OPFEngine;
import net.firejack.platform.api.authority.domain.AuthenticationToken;
import net.firejack.platform.api.content.domain.FileInfo;
import net.firejack.platform.api.filestore.domain.FileStoreInfo;
import net.firejack.platform.core.response.ServiceResponse;
import net.firejack.platform.core.utils.InstallUtils;
import net.firejack.platform.core.utils.OpenFlame;
import net.firejack.platform.web.handler.Builder;
import net.firejack.platform.web.security.x509.KeyUtils;
import org.springframework.beans.BeansException;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Map;

public class FileStoreServiceProxy extends AbstractServiceProxy implements IFileStoreService {

	private String sessionToken;

	public FileStoreServiceProxy(Class[] classes) {
		super(classes);
	}

	@Override
	protected String getSessionToken() {
		if (sessionToken == null) {
			File keystore = InstallUtils.getKeyStore();

			if (!keystore.exists()) return super.getSessionToken();

			try {
				String hostName = InetAddress.getLocalHost().getHostName();
//				String hostName = InetAddress.getLocalHost().getHostName() + "_slave";  //TODO [CLUSTER] don't commit "_slave"

				KeyPair keyPair = KeyUtils.load(keystore);

				if (keyPair == null) {
					throw new IllegalStateException("Key not found");
				}

				X509Certificate certificate = KeyUtils.generateCertificate("", 1, keyPair);
				String cert = new String(Base64.encode(certificate.getEncoded()));

				ServiceResponse<AuthenticationToken> response = OPFEngine.AuthorityService.processSTSCertSignIn(OpenFlame.PACKAGE, hostName, cert);
				if (response.isSuccess()) {
					AuthenticationToken authenticationToken = response.getItem();
					sessionToken = authenticationToken.getToken();
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		return sessionToken;
	}

    @Override
    protected String getClientIp() {
        return sessionToken == null ? super.getClientIp() : null;
    }

    protected void resetSessionToken() {
        sessionToken = null;
    }

    @Override
	public void valid() throws BeansException {
	}

	@Override
	public String getServiceUrlSuffix() {
		return "/filestore";
	}

	@Override
	public ServiceResponse createDirectory(String lookup, String... path) {
		return post("/directory/" + lookup, "path", path);
	}

	@Override
	public ServiceResponse deleteDirectory(String lookup, String... path) {
		return delete("/directory/" + lookup, "path", path);
	}

	@Override
	public ServiceResponse renameDirectory(String lookup, String name, String... path) {
		return put("/directory/" + lookup, "path", path, "name", name);
	}

	@Override
	public ServiceResponse<FileInfo> search(String lookup, String term, String... path) {
		return get("/directory/" + lookup, "path", path, "term", term);
	}

	@Override
	public InputStream download(String lookup, String filename, String... path) {
		return getStream("/file/" + lookup + "/" + filename, "path", path);
	}

	@Override
	public ServiceResponse upload(String lookup, String filename, InputStream inputStream, String... path) {
		return upload1("/file/" + lookup + "/" + filename, inputStream, null, "path", path);
	}

	@Override
	public ServiceResponse deleteFile(String lookup, String filename, String... path) {
		return delete("/file/" + lookup + "/" + filename, "path", path);
	}

	@Override
	public ServiceResponse<FileStoreInfo> readFileStoreInfo() {
		return get("/file");
	}

	@Override
	public ServiceResponse<FileInfo> getInfo(String lookup, String... path) {
		return get("/file/" + lookup, "path", path);
	}

	@Override
	public ServiceResponse zip(String lookup, Map<String, InputStream> stream, String... path) {
		return upload1("/zip/" + lookup, stream, "path", path);
	}

	@Override
	public ServiceResponse updatezip(String lookup, Map<String, InputStream> stream, String... path) {
		return upload2("/zip/" + lookup, stream, "path", path);
	}

	@Override
	public ServiceResponse unzip(String lookup, InputStream stream, String... path) {
		return upload1("/unzip/" + lookup, stream, null, "path", path);
	}

    @Override
	public ServiceResponse<FileInfo> unzipTemp(String lookup, InputStream stream, String... path) {
		return upload1("/unzip/temp/" + lookup, stream, FileInfo.class, "path", path);
	}

    protected <T> T doGet(Builder builder, final Class<T> clazz) {
        return new FileStoreServiceProxyExecutor<T>(this){
            @Override
            public T doRequest(Builder builder) {
                return builder.get(clazz);
            }
        }.request(builder);
    }

    protected <T> T doPost(Builder builder, final Class<T> clazz, final Object part) {
        return new FileStoreServiceProxyExecutor<T>(this){
            @Override
            public T doRequest(Builder builder) {
                return builder.post(clazz, part);
            }
        }.request(builder);
    }

    protected <T> T doPut(Builder builder, final Class<T> clazz, final Object part) {
        return new FileStoreServiceProxyExecutor<T>(this){
            @Override
            public T doRequest(Builder builder) {
                return builder.put(clazz, part);
            }
        }.request(builder);
    }

    protected <T> T doDelete(Builder builder, final Class<T> clazz, final Object part) {
        return new FileStoreServiceProxyExecutor<T>(this){
            @Override
            public T doRequest(Builder builder) {
                return builder.delete(clazz, part);
            }
        }.request(builder);
    }

}
