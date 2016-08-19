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

package com.adaptris.core.services.aggregator;

import java.util.Arrays;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.splitter.SplitByMetadata;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinServiceTest;

public class ReplaceFirstAggregatorTest extends AggregatorCase {

  public ReplaceFirstAggregatorTest(String name) {
    super(name);
  }

  public void testJoinMessage() throws Exception {
    ReplaceWithFirstMessage aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(true);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Goodbye");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("Cruel");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("World");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
    {
        splitMsg1, splitMsg2
    }));
    assertEquals("Cruel", original.getContent());
    // It's part of split message 2 so it gets ignored.
    assertEquals("originalValue", original.getMetadataValue("originalKey"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(SplitJoinServiceTest.wrap(new LogMessageService(), new NullService()));
    service.setSplitter(new SplitByMetadata("metadataKeyToSplitOn", "metadataKeyContainingEachSplitValue"));
    service.setAggregator(new ReplaceWithFirstMessage());
    return service;
  }

  @Override
  protected ReplaceWithFirstMessage createAggregatorForTests() {
    return new ReplaceWithFirstMessage();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\nThis aggregator implementation replaces the original payload with the first logical"
        + "\nmessage that was aggregated"
        + "\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-ReplaceFirstAggregator";
  }
}
