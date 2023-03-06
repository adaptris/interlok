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

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.Alias;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
* Perform Signing.
* <p>
* This may be used as part of a {@link BranchingServiceCollection} if both {@link #setFailId(String)} and
* {@link #setSuccessId(String)} are configured.
* </p>
*
* @config signing-service
*
* @author lchan / $Author: lchan $
*/
@JacksonXmlRootElement(localName = "signing-service")
@XStreamAlias("signing-service")
@AdapterComponent
@ComponentProfile(summary = "Sign a message", tag = "service,security", branchSelector = true)
@DisplayOrder(order = {"localPartner", "remotePartner", "remotePartnerMetadataKey", "encryptionAlgorithm", "keystoreUrls",
"privateKeyPasswordProvider"})
public class SigningService extends EncryptionService {

/**
* @see EncryptionService#doEncryption(byte[], Alias)
*/
protected Output doEncryption(byte[] payload, Alias remoteAlias)
throws AdaptrisSecurityException {
return retrieveSecurityImplementation().sign(payload,
retrieveLocalPartner());
}

}
