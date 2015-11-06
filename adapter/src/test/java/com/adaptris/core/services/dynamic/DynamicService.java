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

import java.util.HashMap;
import java.util.Map;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Test service that records the state.
 * This is purely to test functionality for DynamicServiceLocator.
 * </p>
 */
public class DynamicService extends ServiceImp {
  public static enum State { STATELESS, INIT, STARTED, STOPPED, CLOSE }
  
  private static final Map<Class, State> SERVICE_STATE_MAP = new HashMap<Class, State>();
  
  public DynamicService() {
    super();
    SERVICE_STATE_MAP.put(this.getClass(), State.STATELESS);
  }
  
  /** @see com.adaptris.core.Service
   *   #doService(com.adaptris.core.AdaptrisMessage) */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    ;
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  public void init() throws CoreException {
    SERVICE_STATE_MAP.put(this.getClass(), State.INIT);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  public void close() {
    SERVICE_STATE_MAP.put(this.getClass(), State.CLOSE);
  }
  
  public void start() throws CoreException {
    SERVICE_STATE_MAP.put(this.getClass(), State.STARTED);
  }
  
  
  public void stop() {
    SERVICE_STATE_MAP.put(this.getClass(), State.STOPPED);
  }
 
  public static State currentState(Class clazz) throws Exception {
    if (!SERVICE_STATE_MAP.containsKey(clazz)) {
      throw new Exception(clazz + " not registered");
    }
    return SERVICE_STATE_MAP.get(clazz);
  }

  @Override
  public void prepare() throws CoreException {
  }

}
