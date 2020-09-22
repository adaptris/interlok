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

/**
 * This is just a mock class that ensures that after the first invocation, the
 * same thread is calling it (c.f. Executors.newSingleThreadExecutor() )
 *
 * @author lchan
 *
 */
public class DetectThreadMockMessageProducer extends MockMessageProducer {

  private Thread firstCaller;

  public DetectThreadMockMessageProducer() {
    super();
  }

  @Override
  public void doProduce(AdaptrisMessage msg, String endpoint) throws ProduceException {
    Thread t = Thread.currentThread();
    if (firstCaller == null) {
      firstCaller = t;
    }
    else {
      if (firstCaller != t) {
        throw new ProduceException("Should be the same thread = "
            + firstCaller.getName());
      }
    }
    super.doProduce(msg, endpoint);
  }

}
