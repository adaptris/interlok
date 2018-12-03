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

package com.adaptris.core.services.dynamic;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;

/**
 * <p>
 * Test failing service that records the state. This is purely to test
 * functionality for DynamicServiceLocator.
 * </p>
 */
public class DynamicFailingService extends DynamicService {

  public enum WhenToFail {
    NEVER,
    ON_INIT,
    ON_START,
    ON_PREPARE;
  };

  private WhenToFail whenToFail = WhenToFail.NEVER;

  public DynamicFailingService() {
    super();
  }

  public DynamicFailingService(WhenToFail wtf) {
    this();
    whenToFail = wtf;
  }

  @Override
  protected void initService() throws CoreException {
    if (whenToFail.equals(WhenToFail.ON_INIT)) {
      throw new CoreException(WhenToFail.ON_INIT + " failure specified");
    }
    super.initService();
  }



  @Override
  public void start() throws CoreException {
    if (whenToFail.equals(WhenToFail.ON_START)) {
      throw new CoreException(WhenToFail.ON_START + " failure specified");
    }
    super.start();
  }

  @Override
  public void prepare() throws CoreException {
    if (whenToFail.equals(WhenToFail.ON_PREPARE)) {
      throw new CoreException(WhenToFail.ON_PREPARE.name());
    }
  }

  /**
   * @see com.adaptris.core.Service
   *      #doService(com.adaptris.core.AdaptrisMessage)
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    throw new ServiceException("As Configured");
  }

  public String getWhenToFail() {
    return whenToFail.name();
  }

  public void setWhenToFail(String wtf) {
    this.whenToFail = WhenToFail.valueOf(wtf);
  }

}
