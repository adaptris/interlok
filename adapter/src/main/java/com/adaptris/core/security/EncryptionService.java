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

import java.io.IOException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.security.Output;
import com.adaptris.security.exc.AdaptrisSecurityException;
import com.adaptris.security.keystore.Alias;

/**
 * Base case for performing encryption and signing.
 * 
 * @author lchan / $Author: lchan $
 */
public abstract class EncryptionService extends CoreSecurityService {

  /**
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  @Override
  public final void doService(AdaptrisMessage m) throws ServiceException {

    try {
      Output output = doEncryption(addLength(m), retrieveRemotePartner(m));
      m.setPayload(output.getBytes());
      if (branchingEnabled) {
        m.setNextServiceId(getSuccessId());
      }      
    }
    catch (Exception e) {
      if (branchingEnabled) {
        m.setNextServiceId(getFailId());
        m.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, e);        
      } else {
        throw new ServiceException(e);
      }
    }
  }

  /**
   * Add the v1 length spec to the payload.
   * 
   * @param msg
   *          the AdaptrisMessage
   * @return the byte array with the addition bytes.
   * @throws IOException
   */
  private byte[] addLength(AdaptrisMessage msg) throws IOException {
      return msg.getPayload();
  }

  protected abstract Output doEncryption(byte[] payload,
                                           Alias remoteAlias)
      throws AdaptrisSecurityException;

}
