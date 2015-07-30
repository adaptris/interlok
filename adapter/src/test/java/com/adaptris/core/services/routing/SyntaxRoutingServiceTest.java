/*
 * $RCSfile: SyntaxRoutingServiceTest.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/01/28 17:18:36 $
 * $Author: lchan $
 */
package com.adaptris.core.services.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.SyntaxRoutingServiceExample;
import com.adaptris.core.util.LifecycleHelper;

public class SyntaxRoutingServiceTest extends SyntaxRoutingServiceExample {

  private static final String ROUTING_KEY = "routingKey";
  private static final String POSTCODE_REGEXP_2 = "[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z-[CIKMOV]]{2}";
  private static final String POSTCODE_REGEXP_1 = "[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9][A-Z]{2}";

  public SyntaxRoutingServiceTest(String arg0) {
    super(arg0);
  }

  public void testSetRoutingKey() throws Exception {
    SyntaxRoutingService service = new SyntaxRoutingService();
    try {
      LifecycleHelper.init(service);
      fail("Should not init if routingKey == null");
    }
    catch (CoreException e) {

    }

    service.setRoutingKey(ROUTING_KEY);
    assertEquals(ROUTING_KEY, service.getRoutingKey());
    try {
      service.setRoutingKey(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(ROUTING_KEY, service.getRoutingKey());
    try {
      service.setRoutingKey("");
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(ROUTING_KEY, service.getRoutingKey());
    LifecycleHelper.init(service);
    LifecycleHelper.close(service);
  }

  public void testSetSyntaxIdentifiers() throws Exception {
    SyntaxRoutingService service = new SyntaxRoutingService();
    service.addSyntaxIdentifier(new RegexpSyntaxIdentifier(Arrays.asList(new String[]
    {
        POSTCODE_REGEXP_1, POSTCODE_REGEXP_2
    }), "isPostcode"));
    assertEquals(1, service.getSyntaxIdentifiers().size());
    try {
      service.addSyntaxIdentifier(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(1, service.getSyntaxIdentifiers().size());
    try {
      service.setSyntaxIdentifiers(null);
      fail();
    }
    catch (IllegalArgumentException e) {

    }
    assertEquals(1, service.getSyntaxIdentifiers().size());
  }

  public void testDoServiceFirstMatch() throws Exception {
    SyntaxRoutingService service = new SyntaxRoutingService();
    service.setRoutingKey(ROUTING_KEY);
    service.setSyntaxIdentifiers(createStandardIdentifiers());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("GU34 1ET");
    execute(service, msg);
    assertEquals("isPostcode", msg.getMetadataValue(ROUTING_KEY));
  }

  public void testDoServiceSecondMatch() throws Exception {
    SyntaxRoutingService service = new SyntaxRoutingService();
    service.setRoutingKey(ROUTING_KEY);
    service.setSyntaxIdentifiers(createStandardIdentifiers());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(
        "<document><envelope>ENVELOPE</envelope><content>CONTENT</content></document>");
    execute(service, msg);
    assertEquals("isXml", msg.getMetadataValue(ROUTING_KEY));
  }

  public void testDoServiceNoMatch() throws Exception {
    SyntaxRoutingService service = new SyntaxRoutingService();
    service.setRoutingKey(ROUTING_KEY);
    service.setSyntaxIdentifiers(createStandardIdentifiers());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("The quick brown fox");
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException e) {
      assertEquals("Unable to identify the message syntax for routing", e.getMessage());
    }
  }

  private List<SyntaxIdentifier> createStandardIdentifiers() {
    List<SyntaxIdentifier> result = new ArrayList<SyntaxIdentifier>();
    result.add(new RegexpSyntaxIdentifier(new ArrayList<String>(Arrays.asList(new String[]
        {
            POSTCODE_REGEXP_1, POSTCODE_REGEXP_2
    })), "isPostcode"));
    result.add(new XpathSyntaxIdentifier(new ArrayList<String>(Arrays.asList(new String[]
    {
        "/document/envelope", "/document/content"
    })), "isXml"));
    result.add(new XpathNodeIdentifier(new ArrayList<String>(Arrays.asList(new String[]
    {
      "/document/content/content-list"
    })), "isXml"));
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    SyntaxRoutingService service = new SyntaxRoutingService();
    service.setSyntaxIdentifiers(createStandardIdentifiers());
    service.addSyntaxIdentifier(new AlwaysMatchSyntaxIdentifier("alwaysMatches"));
    service.setRoutingKey(ROUTING_KEY);
    return service;
  }
}
