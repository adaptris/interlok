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

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.util.PlainIdGenerator;

public class MockStandaloneConsumer extends StandaloneConsumer {
  private int startCount = 0, initCount = 0, stopCount = 0, closeCount = 0;

  private String uniqueId = new PlainIdGenerator().create(this);

  public MockStandaloneConsumer(AdaptrisConnection c, AdaptrisMessageConsumer amc) {
    super(c, amc);
    startCount = 0;
  }

  @Override
  public void init() throws CoreException {
    super.init();
    initCount += 1;
  }

  @Override
  public void start() throws CoreException {
    super.start();
    startCount += 1;
  }

  @Override
  public void stop() {
    super.stop();
    stopCount += 1;
  }

  @Override
  public void close() {
    super.close();
    closeCount += 1;
  }

  public int getStartCount() {
    return startCount;
  }

  public int getInitCount() {
    return initCount;
  }

  public int getStopCount() {
    return stopCount;
  }

  public int getCloseCount() {
    return closeCount;
  }

  public String getUniqueId() {
    return uniqueId;
  }
}
