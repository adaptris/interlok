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

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.services.mime.MimeJunitHelper;
import com.adaptris.core.services.splitter.LineCountSplitter;
import com.adaptris.core.services.splitter.MimePartSplitter;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.SplitterCase;
import com.adaptris.core.util.MimeHelper;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.text.mime.BodyPartIterator;

public class MimeAggregatorTest extends MimeAggregatorCase {

  public MimeAggregatorTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testService_ContentEncoding() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(new NullService());
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    MimeAggregator aggr = createAggregatorForTests().withEncoding("base64");
    service.setAggregator(aggr);
    execute(service, msg);
    BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
    assertEquals(11, input.size());
  }

  public void testService_ContentIdProvided() throws Exception {
    // This is a 100 line message, so we expect to get 11 parts.
    AdaptrisMessage msg = SplitterCase.createLineCountMessageInput();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(createAddMetadataService(getName()));
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new LineCountSplitter());
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setPartContentIdMetadataKey(getName());
    service.setAggregator(aggr);
    execute(service, msg);
    BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
    assertEquals(11, input.size());
  }

  public void testService_MimeSplitter() throws Exception {
    // This is a 3 part message, so that should generate 3 split messages; which should generate 4 parts at the end.
    AdaptrisMessage msg = MimeJunitHelper.create();
    SplitJoinService service = new SplitJoinService();
    // The service doesn't actually matter right now.
    service.setService(new NullService());
    service.setTimeout(new TimeInterval(10L, TimeUnit.SECONDS));
    service.setSplitter(new MimePartSplitter());
    MimeAggregator aggr = createAggregatorForTests();
    aggr.setEncoding("base64");
    service.setAggregator(aggr);
    execute(service, msg);
    BodyPartIterator input = MimeHelper.createBodyPartIterator(msg);
    assertEquals(4, input.size());
  }

  @Override
  protected List<Service> retrieveObjectsForSampleConfig() {
    return createExamples(new MimePartSplitter(), new MimeAggregator());
  }

  @Override
  protected MimeAggregator createAggregatorForTests() {
    return new MimeAggregator();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--"
        + "\nConsider the following Multipart Message; (parts are illustrative and may not actually be RFC2045 etc compliant)"
        + "\nContent-ID : 12345" + "\n---BOUNDARY" + "\nContent-Id : abcde" + "\n\nThe quick brown fox jumps over the lazy dog"
        + "\n---BOUNDARY--" + "\nContent-ID : defg" + "\n\nThe five boxing wizards jump quickly" + "\n---BOUNDARY--"
        + "\n\nUsing the MimeSplitter this would generate you 2 split messages; each of which would go through"
        + "\nthe configured service list with the resulting output" + "\n---AnotherBoundary" + "\nContentID: 12345" + "\n"
        + "\n---BOUNDARY" + "\nContent-Id : abcde" + "\n\nThe quick brown fox jumps over the lazy dog" + "\n---BOUNDARY--"
        + "\nContent-ID : defg" + "\n\nThe five boxing wizards jump quickly" + "\n---BOUNDARY--" + "\n--AnotherBoundary--"
        + "\nContent-ID: abcde" + "\n\nThe quick brown fox jumps over the lazy dog" + "\n--AnotherBoundary--"
        + "\nContent-Id : defg" + "\n\nThe five boxing wizards jump quickly" + "\n--AnotherBoundary--" + "\n\n"
        + "\ni.e. the original message is a nested multipart as the first part." + "\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-MimeAggregator";
  }
}
