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

package com.adaptris.core.services.confirmation;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageEventGenerator;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Utilty class which can be extended by <code>Service</code>s which need to
 * confirm confirmations.
 * </p>
 */
public abstract class ConfirmServiceImp extends ServiceImp {

  /**
   * <p>
   * Verifies that a metadata key to obtain the confirmation ID from has been
   * set, and that <code>getIsConfirmation</code> returns true. We could
   * hard code <code>isConfirmation</code> to true, but I think that is 
   * potentially more confusing. 
   * </p> 
   *  @see com.adaptris.core.AdaptrisComponent#init() 
   */
  @Override
  public void init() throws CoreException {
    if (!isConfirmation()) {
      throw new CoreException("isConfirmation must be true");
    }
  }
  
  /**
   * <p>
   * Sets the passed <code>confirmationId</code> against the configured key.
   * </p>
   */
  protected void confirm(AdaptrisMessage msg, String confirmationId) {
    msg.addObjectMetadata
      (MessageEventGenerator.CONFIRMATION_ID_KEY, confirmationId);
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
    // n/a
  }
}
