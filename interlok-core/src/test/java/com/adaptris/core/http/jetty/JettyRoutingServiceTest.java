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

package com.adaptris.core.http.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.services.BranchingServiceExample;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.util.LifecycleHelper;

public class JettyRoutingServiceTest extends BranchingServiceExample {


  @Test
  public void testMatchedRoute() throws Exception {
    JettyRoutingService service = new JettyRoutingService("NotHandled", createRoutes());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, "/record/1234/json");
    msg.addMetadata(CoreConstants.HTTP_METHOD, "GET");
    execute(service, msg);
    assertEquals("handleGet", msg.getNextServiceId());
    assertEquals("1234", msg.getMetadataValue("recId"));
    assertEquals("json", msg.getMetadataValue("outputFormat"));

    // do it twice for _urlPattern compile
    msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, "/record/1234/json");
    msg.addMetadata(CoreConstants.HTTP_METHOD, "GET");
    execute(service, msg);
    assertEquals("handleGet", msg.getNextServiceId());
    assertEquals("1234", msg.getMetadataValue("recId"));
    assertEquals("json", msg.getMetadataValue("outputFormat"));

  }

  @Test
  public void testMatchedRoute_NoMethod() throws Exception {
    JettyRoutingService service = new JettyRoutingService("NotHandled", createRoutes());
    try {
      
      JettyRouteSpec noMethodMatch =
          new JettyRouteSpec().withCondition(new JettyRouteCondition().withUrlPattern("^/record.*$")).withServiceId("listAll");
      service.getRoutes().add(noMethodMatch);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(JettyConstants.JETTY_URI, "/records");
      msg.addMetadata(CoreConstants.HTTP_METHOD, "GET");
      LifecycleHelper.initAndStart(service);
      service.doService(msg);
      assertEquals("listAll", msg.getNextServiceId());

      msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(JettyConstants.JETTY_URI, "/records");
      msg.addMetadata(CoreConstants.HTTP_METHOD, "GET");

      service.doService(msg);
      assertEquals("listAll", msg.getNextServiceId());
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testUnMatchedRoute() throws Exception {
    JettyRoutingService service = new JettyRoutingService("NotHandled", createRoutes());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(JettyConstants.JETTY_URI, "/record/1234");
    msg.addMetadata(CoreConstants.HTTP_METHOD, "TRACE");
    execute(service, msg);
    assertEquals("NotHandled", msg.getNextServiceId());
    assertFalse(msg.headersContainsKey("recId"));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceCollection sl = new BranchingServiceCollection();
    JettyRoutingService service = new JettyRoutingService("NotHandled", createRoutes());
    service.setUniqueId("handleRouting");
    sl.addService(service);
    sl.setFirstServiceId(service.getUniqueId());
    sl.addService(new LogMessageService("handleGet"));
    sl.addService(new LogMessageService("handleDelete"));
    sl.addService(new LogMessageService("handleInsert"));
    sl.addService(new ThrowExceptionService("handleUpdate", new ConfiguredException("cannot handle updates")));
    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    BranchingServiceCollection bs = (BranchingServiceCollection) object;
    JettyRoutingService s = (JettyRoutingService) bs.getServices().get(0);
    return s.getClass().getCanonicalName();
  }

  private List<JettyRouteSpec> createRoutes() {
    List<JettyRouteSpec> result = new ArrayList<>();
    result.add(new JettyRouteSpec()
        .withCondition(new JettyRouteCondition().withUrlPattern("^/record/(.*)$").withMethod("POST").withMetadataKeys("recId"))
        .withServiceId("handleInsert"));
    result.add(new JettyRouteSpec()
        .withCondition(new JettyRouteCondition().withUrlPattern("^/record/(.*)$").withMethod("DELETE").withMetadataKeys("recId"))
        .withServiceId("handleDelete"));
    result.add(new JettyRouteSpec()
        .withCondition(new JettyRouteCondition().withUrlPattern("^/record/(.*)/(.*)$").withMethod("GET").withMetadataKeys("recId",
            "outputFormat"))
        .withServiceId("handleGet"));
    result.add(new JettyRouteSpec()
        .withCondition(new JettyRouteCondition().withUrlPattern("^/record/(.*)$").withMethod("PATCH").withMetadataKeys("recId"))
        .withServiceId("handleUpdate"));
    return result;
  }
}
