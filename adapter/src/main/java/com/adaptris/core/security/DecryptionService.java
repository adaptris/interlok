/*
 * $RCSfile: DecryptionService.java,v $
 * $Revision: 1.16 $
 * $Date: 2008/07/30 08:16:46 $
 * $Author: lchan $
 */
package com.adaptris.core.security;

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
 * @license BASIC
 * @author lchan / $Author: lchan $
 */
@XStreamAlias("decryption-service")
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
