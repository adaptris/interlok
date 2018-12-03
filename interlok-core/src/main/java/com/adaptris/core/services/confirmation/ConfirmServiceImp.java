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

import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MessageEventGenerator;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Utilty class which can be extended by <code>Service</code>s which need to
 * confirm confirmations.
 * </p>
 * 
 * @deprecated since 3.6.2 No-one has ever produced a confirmation service. This will be removed.
 */
@Deprecated
@Removal(version = "3.9.0")
public abstract class ConfirmServiceImp extends ServiceImp {
  
  @Override
  protected void initService() throws CoreException {
    if (!isConfirmation()) {
      throw new CoreException("isConfirmation must be true");
    }
  }

  @Override
  protected void closeService() {}

  /**
   * <p>
   * Sets the passed <code>confirmationId</code> against the configured key.
   * </p>
   */
  protected void confirm(AdaptrisMessage msg, String confirmationId) {
    msg.addObjectHeader
      (MessageEventGenerator.CONFIRMATION_ID_KEY, confirmationId);
  }

}
