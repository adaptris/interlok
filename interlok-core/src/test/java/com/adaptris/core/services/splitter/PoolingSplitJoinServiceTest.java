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

package com.adaptris.core.services.splitter;

import static com.adaptris.core.ServiceCase.asCollection;
import static com.adaptris.core.ServiceCase.execute;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.NullService;
import com.adaptris.core.services.aggregator.MimeAggregator;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.BodyPartIterator;

public class PoolingSplitJoinServiceTest extends SplitJoinServiceTest {

  @Test
  public void testSetMaxThreads() throws Exception {
    PoolingSplitJoinService service = createServiceForTests();
    assertNull(service.getMaxThreads());
    service.setMaxThreads(10);
    assertEquals(10, service.getMaxThreads().intValue());
    service.setMaxThreads(null);
    assertNull(service.getMaxThreads());
  }

  @Test
  public void testService_MaxThreadSpecified() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    PoolingSplitJoinService service = createServiceForTests();
    service.setMaxThreads(8);
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    execute(service, msg);
    BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
    assertEquals(11, input.size());
  }

  @Test
  public void testService_WithWarmStart() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    PoolingSplitJoinService service = createServiceForTests().withWarmStart(true);
    service.setMaxThreads(8);
    // The service doesn't actually matter right now.
    service.setService(asCollection(new NullService()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    service.setAggregator(new MimeAggregator());
    execute(service, msg);
    BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
    assertEquals(11, input.size());
  }
  protected PoolingSplitJoinService createServiceForTests() {
    return new PoolingSplitJoinService();
  }
}
