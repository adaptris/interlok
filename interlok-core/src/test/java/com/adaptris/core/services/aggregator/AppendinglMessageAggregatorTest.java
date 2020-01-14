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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.services.splitter.LineCountSplitter;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;

public class AppendinglMessageAggregatorTest extends AggregatingServiceExample {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Test
  public void testJoinMessage() throws Exception {
    AppendingMessageAggregator aggr = createAggregatorForTests().withOverwriteMetadata(true);
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Goodbye");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(" Cruel ");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("World");
    // should overwrite the value when joined.
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2}));
    assertEquals("Goodbye Cruel World", original.getContent());
    assertEquals("newValue", original.getMetadataValue("originalKey"));
  }

  @Test
  public void testJoin_WithException() {
    AppendingMessageAggregator aggr = createAggregatorForTests();
    AdaptrisMessage original = new DefectiveMessageFactory(EnumSet.of(WhenToBreak.INPUT, WhenToBreak.OUTPUT)).newMessage("Goodbye");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage(" Cruel ");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("World");
    try {
      aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2}));
      fail();
    } catch (CoreException expected) {

    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new LineCountSplitter(2), new AppendingMessageAggregator());
  }

  protected AppendingMessageAggregator createAggregatorForTests() {
    return new AppendingMessageAggregator();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\nThis aggregator implementation simply appends the payloads together."
        + "\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-" + createAggregatorForTests().getClass().getSimpleName();
  }
}
