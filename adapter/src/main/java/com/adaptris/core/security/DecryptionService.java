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
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Perform decryption.
 * 
 * @config decryption-service
 * 
 * @author lchan / $Author: lchan $
 */
@XStreamAlias("decryption-service")
@AdapterComponent
@ComponentProfile(summary = "Decrypt and/or verify the signature of a message", tag = "service,security")
public class DecryptionService extends CoreSecurityService {

  /**
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {

    try {
      Output output = retrieveSecurityImplementation().verify(msg.getPayload(),
          retrieveLocalPartner(), retrieveRemotePartner(msg));

      msg.setPayload(removeLength(output));
      if (branchingEnabled) {
        msg.setNextServiceId(getSuccessId());
      }
    }
    catch (Exception e) {
      if (branchingEnabled) {
        msg.setNextServiceId(getFailId());
        msg.getObjectMetadata().put(CoreConstants.OBJ_METADATA_EXCEPTION, e);
      } else {
        throw new ServiceException(e);
      }
    }
  }

  /**
   * Remove the length specification from the output.
   *
   * @param output the AdaptrisSecurity output.
   * @return the byte array ready for setting as the payload.
   * @throws AdaptrisSecurityException
   */
  private byte[] removeLength(Output output) throws AdaptrisSecurityException {
    return output.getBytes();
  }

}
