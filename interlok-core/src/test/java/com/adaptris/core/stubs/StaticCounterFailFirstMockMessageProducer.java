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
import com.adaptris.core.ProduceException;

public class StaticCounterFailFirstMockMessageProducer extends FailFirstMockMessageProducer {

  private static int failUntilCount = 0;
  private static int produceCount = 0;

  public StaticCounterFailFirstMockMessageProducer() {
    super();
    setFailUntilCount(1);
  }

  @Override
  protected void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    if (msg == null) {
      throw new ProduceException("msg is null");
    }
    if (produceCount < failUntilCount) {
      produceCount++;
      throw new ProduceException("Produce for " + msg.getUniqueId()
          + " deemed to have failed");
    }
    else {
      super.doProduce(msg, endpoint);
    }
  }

  @Override
  public void resetCount() {
    produceCount = 0;
  }

  /**
   * @return the count
   */
  @Override
  public int getFailUntilCount() {
    return failUntilCount;
  }

  /**
   * @param count the count to set
   */
  @Override
  public void setFailUntilCount(int count) {
    failUntilCount = count;
  }
}
