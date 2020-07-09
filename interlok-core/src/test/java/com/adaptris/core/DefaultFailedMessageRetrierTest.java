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

package com.adaptris.core;

import static org.junit.Assert.fail;
import java.util.UUID;
import org.junit.Test;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.stubs.StubEventHandler;

public class DefaultFailedMessageRetrierTest extends FailedMessageRetrierCase {

  @Test
  public void testDuplicateWorkflows() throws Exception {
    DefaultFailedMessageRetrier dfmr = new DefaultFailedMessageRetrier();
    try {
      dfmr.addWorkflow(createWorkflow("t1"));
      dfmr.addWorkflow(createWorkflow("t1"));
      fail("Duplicate workflows should throw an Exception");
    }
    catch (CoreException e) {
      ; // expected.
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = null;
    try {
      DefaultFailedMessageRetrier fmr = new DefaultFailedMessageRetrier();
      FsConsumer consumer = new FsConsumer();
      consumer.setDestination(new ConfiguredConsumeDestination(
          "/path/to/retry-directory"));
      StandaloneConsumer c = new StandaloneConsumer();
      c.setConsumer(consumer);
      fmr.setStandaloneConsumer(c);
      result = new Adapter();
      result.setFailedMessageRetrier(fmr);
      result.setChannelList(new ChannelList());
      result.setEventHandler(new StubEventHandler());
      result.setUniqueId(UUID.randomUUID().toString());
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return DefaultFailedMessageRetrier.class.getCanonicalName();
  }

  @Override
  protected FailedMessageRetrier create() {
    return new DefaultFailedMessageRetrier();
  }

  @Override
  protected DefaultFailedMessageRetrier createForExamples() {
    DefaultFailedMessageRetrier fmr = new DefaultFailedMessageRetrier();
    FsConsumer consumer = new FsConsumer();
    consumer.setBaseDirectoryUrl("/path/to/retry-directory");
    consumer.setEncoder(new MimeEncoder(true, null, null));
    fmr.setStandaloneConsumer(new StandaloneConsumer(consumer));
    return fmr;
  }
}
