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

package com.adaptris.core.services;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.MleMarker;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceList;
import com.adaptris.core.stubs.MockConfirmService;
import com.adaptris.core.stubs.MockSetUpConfirmationService;
import com.adaptris.core.util.LifecycleHelper;

@SuppressWarnings("deprecation")
public class ExampleConfirmationServicesTest extends BaseCase {

  private MockSetUpConfirmationService setUpConfirmationService;
  private MockConfirmService confirmService;

  public ExampleConfirmationServicesTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    setUpConfirmationService = new MockSetUpConfirmationService();

    confirmService = new MockConfirmService();
    confirmService.setIsConfirmation(true);
  }

  public void testExampleConfirmationServices() throws Exception {

    ServiceCollection setUp = new ServiceList();
    setUp.addService(setUpConfirmationService);

    ServiceCollection confirm = new ServiceList();
    confirm.addService(confirmService);

    LifecycleHelper.init(confirm);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage();

    ServiceCase.execute(setUp, msg);

    List mleMarkers = msg.getMessageLifecycleEvent().getMleMarkers();

    assertEquals(1, mleMarkers.size());
    String confId = ((MleMarker) mleMarkers.get(0)).getConfirmationId();

    assertNotNull(confId);

    AdaptrisMessage confirmation = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(confId);
    ServiceCase.execute(confirm, confirmation);

    // confirm.doService(confirmation);

    mleMarkers = confirmation.getMessageLifecycleEvent().getMleMarkers();

    assertEquals(1, mleMarkers.size());
    confId = ((MleMarker) mleMarkers.get(0)).getConfirmationId();

    assertNotNull(confId);
    assertTrue(((MleMarker) mleMarkers.get(0)).getIsConfirmation());
  }
}
