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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.event.AdapterCloseEvent;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class MarshalledClassDestinationTest
    extends com.adaptris.interlok.junit.scaffolding.ExampleProduceDestinationCase {

  private static final String CHANNEL_DESTINATION = "channel-destination";
  private static final String SERVICE_LIST_DESTINATION = "service-list-destination";
  private static final String DEFAULT_DESTINATION = "default-destination";
  private static final String ADAPTER_DESTINATION = "adapter-destination";

  private AdaptrisMarshaller marshaller;


  @Before
  public void setUp() throws Exception {
    marshaller = DefaultMarshaller.getDefaultMarshaller();
  }

  @Test
  public void testGetDestination() throws Exception {
    AdaptrisMessage msg = createMessage(new Adapter());
    ProduceDestination d = createDestination();
    String s = d.getDestination(msg);
    assertEquals(ADAPTER_DESTINATION, s);
  }

  @Test
  public void testGetDestination_ExplicitMarshaller() throws Exception {
    MarshalledClassDestination d = createDestination();
    d.setMarshaller(DefaultMarshaller.getDefaultMarshaller());
    AdaptrisMessage msg = createMessage(new Adapter());
    String s = d.getDestination(msg);
    assertEquals(ADAPTER_DESTINATION, s);
  }

  @Test
  public void testBadPayload() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABCDE");
    ProduceDestination d = createDestination();
    String s = d.getDestination(msg);
    assertEquals(DEFAULT_DESTINATION, s);
  }

  @Test
  public void testNoDefaultDestination() throws Exception {
    AdaptrisMessage msg = createMessage(new MockChannel());
    ProduceDestination d = createDestination();
    ((MarshalledClassDestination) d).setDefaultDestination("");
    try {
      d.getDestination(msg);
      fail("With no default dest, should throw RuntimeException");
    }
    catch (RuntimeException e) {
      assertEquals("Could not resolve destination", e.getMessage());
    }
  }

  @Test
  public void testDefaultDestination() throws Exception {
    AdaptrisMessage msg = createMessage(EventFactory.create(AdapterCloseEvent.class));
    ProduceDestination d = createDestination();
    String s = d.getDestination(msg);
    assertEquals(DEFAULT_DESTINATION, s);
  }

  private MarshalledClassDestination createDestination() {
    MarshalledClassDestination d = new MarshalledClassDestination();
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.addKeyValuePair(new KeyValuePair(ServiceList.class.getName(), SERVICE_LIST_DESTINATION));
    kvps.addKeyValuePair(new KeyValuePair(Adapter.class.getName(), ADAPTER_DESTINATION));
    kvps.addKeyValuePair(new KeyValuePair(Channel.class.getName(), CHANNEL_DESTINATION));
    d.setDefaultDestination(DEFAULT_DESTINATION);
    d.setClassMappings(kvps);
    return d;
  }

  private AdaptrisMessage createMessage(Object c) throws Exception {
    return AdaptrisMessageFactory.getDefaultInstance().newMessage(marshaller.marshal(c));

  }

  @Override
  protected ProduceDestination createDestinationForExamples() {
    return createDestination();
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object)
        + "<!--\n\nThis ProduceDestination implementation derives its destination using an AdaptrisMarshaller."
        + "\nThe AdaptrisMessage is unmarshalled into an object, and the associated class matched against the"
        + "\nconfigured list. If a match is found the string associated with this class name is used as the destination."
        + "\nIf no match is found then the default destination will be used."
        + "\n\nThis destination is not useful unless the message is a serialized object which requires routing."
        + "\nIt is used internally to handle serialised XML events. " + "\n\n-->\n";
  }

}
