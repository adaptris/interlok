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

package com.adaptris.core.stubs;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.confirmation.SetUpConfirmationServiceImp;

/**
 * <p>
 * Example <code>Service</code> which sets up a confirmation using
 * the current time as the confirmation ID.
 * </p>
 */
@Deprecated
public class MockSetUpConfirmationService extends
    SetUpConfirmationServiceImp {

  /** 
   * @see com.adaptris.core.Service#doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    
    this.registerConfirmationId
      (msg, new Long(System.currentTimeMillis()).toString());
  }

  @Override
  public void prepare() throws CoreException {
  }

}
