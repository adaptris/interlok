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

package com.adaptris.core.event;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMarshaller;
import com.adaptris.core.BaseCase;
import com.adaptris.core.DefaultMarshaller;


public class StandardAdapterStartUpEventTest extends BaseCase {

  private static final String LEGACY_EVENT_XML = "<standard-adapter-start-up-event>" + "  <unique-id>123456</unique-id>"
      + "  <creation-time>1431347295942</creation-time>" + "  <was-successful>true</was-successful>"
      + "  <compressed-adapter-xml>...</compressed-adapter-xml>" + "  <adapter>...</adapter>"
      + "</standard-adapter-start-up-event>";
  public StandardAdapterStartUpEventTest(java.lang.String testName) {
    super(testName);
  }

  public void testSetAdapter() throws Exception {
    StandardAdapterStartUpEvent evt = new StandardAdapterStartUpEvent();
    Adapter adapter = new Adapter();
    evt.setAdapter(adapter);
  }

  public void testMarshalledHasNoAdapter() throws Exception {
    StandardAdapterStartUpEvent event = new StandardAdapterStartUpEvent();
    Adapter adapter = new Adapter();
    event.setAdapter(adapter);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String s = m.marshal(event);
    System.out.println("Event marshalled - " + s);
    StandardAdapterStartUpEvent mEvent = (StandardAdapterStartUpEvent) m.unmarshal(s);
    System.out.println("mEvent unmarshalled - " + mEvent.toString());
  }

  public void testLegacyUnmarshal() throws Exception {
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    StandardAdapterStartUpEvent evt = (StandardAdapterStartUpEvent) m.unmarshal(LEGACY_EVENT_XML);
    assertEquals("123456", evt.getUniqueId());
  }
}
