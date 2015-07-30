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
      addCompatibilityMetadata(m);
      if (branchingEnabled) {
        m.setNextServiceId(getSuccessId());
      }      
    }
    catch (Exception e) {
      if (branchingEnabled) {
        m.setNextServiceId(getFailId());
        m.getObjectMetadata().put(CoreConstants.OBJ_METADATA_EXCEPTION, e);        
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

  private void addCompatibilityMetadata(AdaptrisMessage msg) {
      return;
  }

  protected abstract Output doEncryption(byte[] payload,
                                           Alias remoteAlias)
      throws AdaptrisSecurityException;

}
