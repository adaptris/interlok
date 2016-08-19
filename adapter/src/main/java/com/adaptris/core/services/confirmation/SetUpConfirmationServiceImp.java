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
 * set up confirmations.
 * </p>
 */
public abstract class SetUpConfirmationServiceImp extends ServiceImp {

  /**
   * <p>
   * Sets the passed <code>confirmationId</code> against the configured key.
   * </p>
   */
  protected void registerConfirmationId
    (AdaptrisMessage msg, String confirmationId) {
    
    msg.addObjectHeader
      (MessageEventGenerator.CONFIRMATION_ID_KEY, confirmationId);
  }
  
  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {}
}
