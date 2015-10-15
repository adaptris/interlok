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

import static com.adaptris.core.services.splitter.XpathSplitterTest.ENCODING_UTF8;
import static com.adaptris.core.services.splitter.XpathSplitterTest.ENVELOPE_DOCUMENT;

import java.util.Arrays;

import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceList;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.splitter.SplitJoinService;
import com.adaptris.core.services.splitter.XpathMessageSplitter;
import com.adaptris.util.text.xml.InsertNode;

public abstract class AggregatorCase extends AggregatingServiceExample {

  private static final String XPATH_ENVELOPE = "/envelope";

  public AggregatorCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected void tearDown() throws Exception {
  }

  public void testSetOverwriteMetadata() throws Exception {
    MessageAggregatorImpl impl = createAggregatorForTests();
    assertNull(impl.getOverwriteMetadata());
    assertEquals(false, impl.overwriteMetadata());
    impl.setOverwriteMetadata(Boolean.TRUE);
    assertEquals(Boolean.TRUE, impl.getOverwriteMetadata());
    assertEquals(true, impl.overwriteMetadata());
    impl.setOverwriteMetadata(null);
    assertNull(impl.getOverwriteMetadata());
    assertEquals(false, impl.overwriteMetadata());
  }

  protected abstract MessageAggregatorImpl createAggregatorForTests();

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SplitJoinService service = new SplitJoinService();
    service.setService(new ServiceList(Arrays.asList(new Service[]
    {
        new LogMessageService(), new NullService()
    })));
    service.setSplitter(new XpathMessageSplitter(ENVELOPE_DOCUMENT, ENCODING_UTF8));
    service.setAggregator(new XmlDocumentAggregator(new InsertNode(XPATH_ENVELOPE)));
    return service;
  }

}
