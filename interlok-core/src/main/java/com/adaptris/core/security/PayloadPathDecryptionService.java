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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Decrypt part of a message using a configurable path.
 *
 * @config payload-path-decryption-service
 *
 * @author jwickham / $Author: jwickham $
 */
@XStreamAlias("payload-path-decryption-service")
@AdapterComponent
@ComponentProfile(summary = "Decrypt part of a message using a configurable path", tag = "service,security,path", since = "5.0.0")
@DisplayOrder(order = { "localPartner", "remotePartner", "remotePartnerMetadataKey", "encryptionAlgorithm", "keystoreUrls",
    "privateKeyPasswordProvider" })

public class PayloadPathDecryptionService extends CoreSecurityService {

  private static final String EXCEPTION_MESSAGE = "Failed to decrypt message";

  @NotNull
  @Valid
  @NonNull
  @Getter
  @Setter
  private PathBuilder pathBuilder;

  public PayloadPathDecryptionService() {
    pathBuilder = new XpathBuilder();
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    Map<String, String> pathKeyValuePairs = getPathBuilder().extract(msg);

    for (Map.Entry<String, String> entry : pathKeyValuePairs.entrySet()) {
      String xpathKey = entry.getKey();
      String xpathValue = entry.getValue();
      try {
        Output output = retrieveSecurityImplementation().verify(xpathValue.getBytes(), retrieveLocalPartner(), retrieveRemotePartner(msg));
        pathKeyValuePairs.put(xpathKey, output.getAsString());
      } catch (AdaptrisSecurityException e) {
        throw new ServiceException(EXCEPTION_MESSAGE, e);
      }

      getPathBuilder().insert(msg, pathKeyValuePairs);
    }
  }

}
