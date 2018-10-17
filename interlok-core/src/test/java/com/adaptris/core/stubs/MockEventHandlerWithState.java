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

import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class MockEventHandlerWithState extends DefaultEventHandler {

  private String uniqueId;

  /**
   * @throws CoreException
   */
  public MockEventHandlerWithState() throws CoreException {
    super();
    setProducer(new MockMessageProducer());
  }

  public MockEventHandlerWithState(String id) throws CoreException {
    this();
    uniqueId = id;
  }

  @Override
  public String toString() {
    return uniqueId != null ? uniqueId : super.toString();
  }
}
