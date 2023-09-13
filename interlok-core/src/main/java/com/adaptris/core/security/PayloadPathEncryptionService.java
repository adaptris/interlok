/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.security;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.Args;
import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.NonNull;

/**
 * Perform encryption on part of a message using a configurable path.
 * 
 * @config payload-path-encryption-service
 * 
 * @author jwickham / $Author: jwickham $
 */
@XStreamAlias("payload-path-encryption-service")
@AdapterComponent
@ComponentProfile(summary = "Encrypt part of a message using a configurable path", tag = "service,security,path", since = "5.0.0")
@DisplayOrder(order = { "localPartner", "remotePartner", "remotePartnerMetadataKey", "encryptionAlgorithm",
    "keystoreUrls", "privateKeyPasswordProvider" })

public class PayloadPathEncryptionService extends CoreSecurityService {
  
  private static final String EXCEPTION_MESSAGE = "Failed to encrypt message";

  @NotNull
  @NonNull
  private PathBuilder pathBuilder;
  
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Map<String, String> pathKeyValuePairs;
    pathKeyValuePairs = getPathBuilder().extract(msg);

    for (Map.Entry<String, String> entry : pathKeyValuePairs.entrySet()) {
      String xpathKey = entry.getKey();
      String xpathValue = entry.getValue();
      try {
        Output output = retrieveSecurityImplementation().encrypt(xpathValue.getBytes(), retrieveLocalPartner(),
            retrieveRemotePartner(msg));
        pathKeyValuePairs.put(xpathKey, output.getAsString());
      } catch (AdaptrisSecurityException e) {
        e.printStackTrace();
        throw new ServiceException(EXCEPTION_MESSAGE);
      }
      
      getPathBuilder().insert(msg, pathKeyValuePairs);
    }
  }
  
  public PathBuilder getPathBuilder() {
    return pathBuilder;
  }

  public void setPathBuilder(PathBuilder pathBuilder) {
    this.pathBuilder = Args.notNull(pathBuilder, "pathBuilder");
  }

}
