package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.StateManagedComponent;

// This is all just a bit of fakery to get 100% (ha ha).
public class LoggingHelperTest extends LoggingHelper {


  @Test
  public void testFriendlyNameService() throws Exception {
    assertEquals("", friendlyName((Service) null));
    NullService service = new NullService();
    assertEquals("NullService", friendlyName(service));
    service.setUniqueId("");
    assertEquals("NullService", friendlyName(service));
    service.setUniqueId("testFriendlyNameService");
    assertEquals("NullService(testFriendlyNameService)", friendlyName(service));
  }


  @Test
  public void testFriendlyNameStateManagedComponent() throws Exception {
    assertEquals("", friendlyName((StateManagedComponent) null));
    NullService service = new NullService();
    assertEquals("NullService", friendlyName((StateManagedComponent) service));
    service.setUniqueId("testFriendlyNameService");
    assertEquals("NullService(testFriendlyNameService)", friendlyName((StateManagedComponent) service));
  }

  @Test
  public void testFriendlyNameComponentLifecycle() throws Exception {
    assertEquals("", friendlyName((ComponentLifecycle) null));
    NullService service = new NullService();
    assertEquals("NullService", friendlyName((ComponentLifecycle) service));
    service.setUniqueId("testFriendlyNameService");
    assertEquals("NullService(testFriendlyNameService)", friendlyName((ComponentLifecycle) service));
    ComponentLifecycle mock = mock(ComponentLifecycle.class);
    assertNotNull(friendlyName(mock));
  }

}
