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
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;

/**
 * <p>
 * Example branching <code>Service</code>.  Selects a configurable ID for the 
 * next <code>Service</code> to apply based on a whether a random number is
 * greater than or less than 0.5.
 * </p>
 */
public class ExampleBranchingService extends BranchingServiceImp {
  
  private String lowerServiceId;
  private String higherServiceId;

  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    if (Math.random() < 0.5) {
      msg.setNextServiceId(lowerServiceId);
    }
    else {
      msg.setNextServiceId(higherServiceId);
    }
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    // na
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    // na
  }
  
  /**
   * <p>
   * Returns the unique Id of the next <code>Service</code> to apply if the 
   * random number is greater than 0.5.
   * </p>
   * @return the unique Id of the next <code>Service</code> to apply if the 
   * random number is greater than 0.5
   */
  public String getHigherServiceId() {
    return higherServiceId;
  }

  /**
   * <p>
   * Returns the unique Id of the next <code>Service</code> to apply if the 
   * random number is less than 0.5.
   * </p>
   * @return the unique Id of the next <code>Service</code> to apply if the 
   * random number is less than 0.5
   */
  public String getLowerServiceId() {
    return lowerServiceId;
  }

  /**
   * <p>
   * Sets the unique Id of the next <code>Service</code> to apply if the 
   * random number is greater than 0.5.
   * </p>
   * @param string the unique Id of the next <code>Service</code> to apply if 
   * the random number is greater than 0.5
   */
  public void setHigherServiceId(String string) {
    higherServiceId = string;
  }

  /**
   * <p>
   * Sets the unique Id of the next <code>Service</code> to apply if 
   * the random number is less than 0.5.
   * </p>
   * @param string the unique Id of the next <code>Service</code> to 
   * apply if the random number is less than 0.5
   */
  public void setLowerServiceId(String string) {
    lowerServiceId = string;
  }
}
