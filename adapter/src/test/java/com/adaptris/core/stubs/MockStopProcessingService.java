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
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.license.License;

/**
 * <p>
 * Implementation of <code>Service</code> for testing 'stop-processing' 
 * functionality.  Sets metadata such that any services configured after this 
 * one will be ignored.
 * </p>
 */
public class MockStopProcessingService extends ServiceImp {

  /**
   * <p>
   * Sets 'break-out' metadata for testing.
   * </p>
   * @param msg the message to apply service to
   * @throws ServiceException wrapping any underlying <code>Exception</code>s
   */
  public void doService(AdaptrisMessage msg) 
    throws ServiceException {
    
    msg.addMetadata
      (CoreConstants.STOP_PROCESSING_KEY, CoreConstants.STOP_PROCESSING_VALUE);
  }

  
  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() { 
    // na
  }
  
  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() { 
    // na
  }

  /** @see java.lang.Object#toString() */
  public String toString() {
    StringBuffer result = new StringBuffer();

    result.append("[");
    result.append(this.getClass().getName());
    result.append("] ");

    return result.toString();
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return true;
  }

}
