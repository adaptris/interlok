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

package com.adaptris.core.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.util.TimeInterval;

public abstract class ActiveJmsConnectionErrorHandlerCase {

  @Rule
  public TestName testName = new TestName();
  protected Logger log = LoggerFactory.getLogger(this.getClass());

  @Test
  public void testRetryInterval() {
    ActiveJmsConnectionErrorHandler handler = new ActiveJmsConnectionErrorHandler();
    assertNull(handler.getCheckInterval());
    assertEquals(5000, handler.retryInterval());

    TimeInterval interval = new TimeInterval(1L, TimeUnit.MINUTES);
    TimeInterval bad = new TimeInterval(0L, TimeUnit.MILLISECONDS);

    handler.setCheckInterval(interval);
    assertEquals(interval, handler.getCheckInterval());
    assertEquals(interval.toMilliseconds(), handler.retryInterval());

    handler.setCheckInterval(bad);
    assertEquals(bad, handler.getCheckInterval());
    assertEquals(5000, handler.retryInterval());

    handler.setCheckInterval(null);
    assertNull(handler.getCheckInterval());
    assertEquals(5000, handler.retryInterval());
  }

  @Test
  public void testAdditionalLogging() {
    ActiveJmsConnectionErrorHandler ajceh = new ActiveJmsConnectionErrorHandler();
    assertNull(ajceh.getAdditionalLogging());
    assertFalse(ajceh.additionalLogging());
    ajceh.setAdditionalLogging(Boolean.TRUE);
    assertNotNull(ajceh.getAdditionalLogging());
    assertEquals(true, ajceh.additionalLogging());
    assertEquals(Boolean.TRUE, ajceh.getAdditionalLogging());
    ajceh.setAdditionalLogging(null);
    assertNull(ajceh.getAdditionalLogging());
    assertFalse(ajceh.additionalLogging());
  }
}
