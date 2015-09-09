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
