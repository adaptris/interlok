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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class AdapterStateSummaryTest extends BaseCase {
  private static final String DEFAULT_KEY = "DEFAULT";

  public AdapterStateSummaryTest() {
  }

  @Test
  public void testAdapterStateSummary() throws Exception {

    assertNotNull(new AdapterStateSummary());
  }

  @Test
  public void testAdapterStateSummaryAdapter() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId(DEFAULT_KEY);
    a.getChannelList().addChannel(new Channel());
    AdapterStateSummary summary = new AdapterStateSummary(a);
    assertNotNull(summary);
    assertNotNull(summary.getLastStopTime());
    assertNull(summary.getLastStartTime());
    assertEquals(DEFAULT_KEY, summary.getAdapterState().getKey());
    assertEquals(ClosedState.class.getName(), summary.getAdapterState().getValue());
    assertEquals(0, summary.getChannelStates().size());
  }

  @Test
  public void testAdapterStateSummaryAdapterInit() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId(DEFAULT_KEY);
    a.getChannelList().addChannel(new Channel());
    LifecycleHelper.init(a);
    AdapterStateSummary summary = new AdapterStateSummary(a);
    assertEquals(DEFAULT_KEY, summary.getAdapterState().getKey());
    assertEquals(InitialisedState.class.getName(), summary.getAdapterState().getValue());
    assertNotNull(summary.getLastStopTime());
    assertNull(summary.getLastStartTime());
  }

  @Test
  public void testAdapterStateSummaryAdapterStart() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId(DEFAULT_KEY);
    a.getChannelList().addChannel(new Channel());
    LifecycleHelper.init(a);
    LifecycleHelper.start(a);
    AdapterStateSummary summary = new AdapterStateSummary(a);
    assertEquals(DEFAULT_KEY, summary.getAdapterState().getKey());
    assertEquals(StartedState.class.getName(), summary.getAdapterState().getValue());
    assertNotNull(summary.getLastStopTime());
    assertNotNull(summary.getLastStartTime());
  }

  @Test
  public void testSetAdapterStateStringComponentState() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId(DEFAULT_KEY);
    AdapterStateSummary s1 = new AdapterStateSummary(a);
    s1.setAdapterState(DEFAULT_KEY, ClosedState.getInstance());
    assertEquals(DEFAULT_KEY, s1.getAdapterState().getKey());
    assertEquals(ClosedState.getInstance().getClass().getName(), s1.getAdapterState().getValue());
    s1.setAdapterState("", ClosedState.getInstance());
    assertEquals(DEFAULT_KEY, s1.getAdapterState().getKey());
    assertEquals(ClosedState.getInstance().getClass().getName(), s1.getAdapterState().getValue());
    s1.setAdapterState(null, ClosedState.getInstance());
    assertEquals(DEFAULT_KEY, s1.getAdapterState().getKey());
    assertEquals(ClosedState.getInstance().getClass().getName(), s1.getAdapterState().getValue());
    try {
      s1.setAdapterState(DEFAULT_KEY, null);
      fail("Null Component State");
    }
    catch (IllegalArgumentException expected) {

    }
  }

  @Test
  public void testSetAdapterStateKeyValuePair() {
    AdapterStateSummary s1 = new AdapterStateSummary();
    s1.setAdapterState(new KeyValuePair(DEFAULT_KEY, ClosedState.getInstance().getClass().getName()));
    assertEquals(DEFAULT_KEY, s1.getAdapterState().getKey());
    assertEquals(ClosedState.getInstance().getClass().getName(), s1.getAdapterState().getValue());
    try {
      s1.setAdapterState(new KeyValuePair("", "ABC"));
      fail("Null Key in KeyValuePair");
    }
    catch (IllegalArgumentException expected) {

    }
    try {
      s1.setAdapterState(new KeyValuePair("ABC", ""));
      fail("Null Value in KeyValuePair");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      s1.setAdapterState(null);
      fail("Null KeyValuePair");
    }
    catch (IllegalArgumentException expected) {

    }
  }


  @Test
  public void testAddChannelStateStringComponentState() {
    AdapterStateSummary s1 = new AdapterStateSummary();
    s1.addChannelState(DEFAULT_KEY, ClosedState.getInstance());
    assertEquals(1, s1.getChannelStates().size());
    assertTrue(s1.getChannelStates().contains(new KeyValuePair(DEFAULT_KEY, "")));
    assertEquals(DEFAULT_KEY, s1.getChannelStates().getKeyValuePair(DEFAULT_KEY).getKey());
    assertEquals(ClosedState.getInstance().getClass().getName(), s1.getChannelStates().getKeyValuePair(DEFAULT_KEY).getValue());
    s1.addChannelState("", ClosedState.getInstance());
    assertEquals(1, s1.getChannelStates().size());
    s1.addChannelState(null, ClosedState.getInstance());
    assertEquals(1, s1.getChannelStates().size());
    try {
      s1.addChannelState(DEFAULT_KEY, null);
      fail("Null Component State");
    }
    catch (IllegalArgumentException expected) {

    }

  }


  @Test
  public void testAddChannelStateKeyValuePair() {
    AdapterStateSummary s1 = new AdapterStateSummary();
    s1.addChannelState(new KeyValuePair(DEFAULT_KEY, ClosedState.getInstance().getClass().getName()));
    assertEquals(1, s1.getChannelStates().size());
    assertTrue(s1.getChannelStates().contains(new KeyValuePair(DEFAULT_KEY, "")));
    assertEquals(DEFAULT_KEY, s1.getChannelStates().getKeyValuePair(DEFAULT_KEY).getKey());
    assertEquals(ClosedState.getInstance().getClass().getName(), s1.getChannelStates().getKeyValuePair(DEFAULT_KEY).getValue());
    s1.addChannelState(new KeyValuePair("", "ABC"));
    assertEquals(1, s1.getChannelStates().size());
    try {
      s1.addChannelState(new KeyValuePair("ABC", ""));
      fail("Null Value in KeyValuePair");
    }
    catch (IllegalArgumentException expected) {
    }
    try {
      s1.addChannelState(null);
      fail("Null KeyValuePair");
    }
    catch (IllegalArgumentException expected) {

    }
  }


  @Test
  public void testGetChannelStates() {
    AdapterStateSummary s1 = new AdapterStateSummary();
    assertEquals(0, s1.getChannelStates().size());
  }

  @Test
  public void testSetChannelStates() {
    AdapterStateSummary s1 = new AdapterStateSummary();
    s1.setChannelStates(new KeyValuePairSet());
    assertEquals(0, s1.getChannelStates().size());
  }


  @Test
  public void testToString() {
    AdapterStateSummary s1 = new AdapterStateSummary();
    s1.addChannelState(new KeyValuePair(DEFAULT_KEY, ClosedState.getInstance().getClass().getName()));
    assertNotNull(s1.toString());
  }

  @Test
  public void testRoundTrip() throws Exception {
    Adapter a = new Adapter();
    a.setUniqueId(DEFAULT_KEY);
    AdapterStateSummary s1 = new AdapterStateSummary(a);
    AdaptrisMarshaller cm = DefaultMarshaller.getDefaultMarshaller();
    String xml = cm.marshal(s1);
    AdapterStateSummary s2 = (AdapterStateSummary) cm.unmarshal(xml);
    assertRoundtripEquality(s1, s2);
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

}
