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

import org.junit.Test;

@SuppressWarnings("deprecation")
public class DefaultAdapterStartUpEventTest {

  public DefaultAdapterStartUpEventTest() {
  }

  @Test
  public void testSetAdapter() throws Exception {
    DefaultAdapterStartUpEvent evt = new DefaultAdapterStartUpEvent();
    Adapter adapter = new Adapter();
    evt.setAdapter(adapter);
  }

  @Test
  public void testMarshalledHasNoAdapter() throws Exception {
    DefaultAdapterStartUpEvent event = new DefaultAdapterStartUpEvent();
    Adapter adapter = new Adapter();
    event.setAdapter(adapter);
    AdaptrisMarshaller m = DefaultMarshaller.getDefaultMarshaller();
    String s = m.marshal(event);
    System.out.println("Event marshalled - " + s);
    DefaultAdapterStartUpEvent mEvent = (DefaultAdapterStartUpEvent) m.unmarshal(s);
    System.out.println("mEvent unmarshalled - " + mEvent.toString());
  }
}
