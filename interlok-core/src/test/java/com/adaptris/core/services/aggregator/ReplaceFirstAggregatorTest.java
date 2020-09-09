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
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.services.conditional.conditions.ConditionImpl;
import com.adaptris.core.services.splitter.SplitByMetadata;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;

public class ReplaceFirstAggregatorTest extends AggregatorCase {


  @Test
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

  @Test
  public void testJoinMessageWithFilter() throws Exception {
    ReplaceWithFirstMessage aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(true);
    aggr.setFilterCondition(new LengthCheckCondition());
    AdaptrisMessage original = AdaptrisMessageFactory.getDefaultInstance().newMessage("Goodbye");
    original.addMetadata("originalKey", "originalValue");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("short");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("justShort");
    AdaptrisMessage splitMsg3 = AdaptrisMessageFactory.getDefaultInstance().newMessage("ofSufficientLength");
    AdaptrisMessage splitMsg4 = AdaptrisMessageFactory.getDefaultInstance().newMessage("tooSmall");
    splitMsg2.addMetadata("originalKey", "newValue");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[]
            {
                    splitMsg1, splitMsg2, splitMsg3, splitMsg4
            }));
    assertEquals("ofSufficientLength", original.getContent());
    // It's part of split message 2 so it gets ignored.
    assertEquals("originalValue", original.getMetadataValue("originalKey"));
  }

  @Test(expected = CoreException.class)
  public void testAggregate_BrokenOutput() throws Exception {
    ReplaceWithFirstMessage aggr = createAggregatorForTests();
    aggr.setOverwriteMetadata(true);
    AdaptrisMessage original =
        new DefectiveMessageFactory(WhenToBreak.OUTPUT).newMessage("Goodbye");
    AdaptrisMessage splitMsg1 = AdaptrisMessageFactory.getDefaultInstance().newMessage("short");
    AdaptrisMessage splitMsg2 = AdaptrisMessageFactory.getDefaultInstance().newMessage("justShort");
    AdaptrisMessage splitMsg3 = AdaptrisMessageFactory.getDefaultInstance().newMessage("ofSufficientLength");
    AdaptrisMessage splitMsg4 = AdaptrisMessageFactory.getDefaultInstance().newMessage("tooSmall");
    aggr.joinMessage(original, Arrays.asList(new AdaptrisMessage[] {splitMsg1, splitMsg2, splitMsg3, splitMsg4}));
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new SplitByMetadata("metadataKeyToSplitOn", "metadataKeyContainingEachSplitValue"),
        new ReplaceWithFirstMessage());
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

  private class LengthCheckCondition extends ConditionImpl {
    @Override
    public boolean evaluate(AdaptrisMessage message) throws CoreException {
      return message.getContent().length() > 10;
    }

    @Override
    public void close() {
      throw new RuntimeException();
    }
  }
}
