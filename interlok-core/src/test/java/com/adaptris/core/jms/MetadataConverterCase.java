package com.adaptris.core.jms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.jms.activemq.EmbeddedActiveMq;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.metadata.RegexMetadataFilter;

/**
 * @author mwarman
 */
public abstract class MetadataConverterCase {

  
  
  
  protected static EmbeddedActiveMq activeMqBroker;

  @BeforeAll
  public static void setUpAll() throws Exception {
    activeMqBroker = new EmbeddedActiveMq();
    activeMqBroker.start();
  }
  
  @AfterAll
  public static void tearDownAll() throws Exception {
    if(activeMqBroker != null)
      activeMqBroker.destroy();
  }

  static final String HEADER = "header";

  @Test
  public void testMetadataFilter() throws Exception {
    MetadataConverter mc = createConverter();
    assertTrue(mc.getMetadataFilter() instanceof NoOpMetadataFilter);
    mc.setMetadataFilter(new RegexMetadataFilter());
    assertTrue(mc.getMetadataFilter() instanceof RegexMetadataFilter);
  }

  @Test
  public void testStrict() throws Exception {
    MetadataConverter mc = createConverter();
    assertFalse(mc.strict());
    assertNull(mc.getStrictConversion());
    mc.setStrictConversion(false);
    assertFalse(mc.strict());
    assertEquals(Boolean.FALSE, mc.getStrictConversion());
  }

  @Test
  public void testSetProperty() throws Exception {
    MetadataConverter mc = createConverter();
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    MetadataCollection metadataCollection = new MetadataCollection();
    metadataCollection.add(new MetadataElement(HEADER, getStringValue()));
    Message jmsMsg = session.createMessage();
    mc.moveMetadata(metadataCollection, jmsMsg);
    assertEquals(getStringValue(), jmsMsg.getStringProperty(HEADER));
    assertValue(jmsMsg);
  }

  @Test
  public void testSetPropertyWithReserved() throws Exception {
    MetadataConverter mc = createConverter();
    Session session = activeMqBroker.createConnection().createSession(false, Session.CLIENT_ACKNOWLEDGE);
    MetadataCollection metadataCollection = new MetadataCollection();
    metadataCollection.add(new MetadataElement(HEADER, getStringValue()));
    metadataCollection.add(new MetadataElement("JMSCorrelationID", "1234"));
    Message jmsMsg = session.createMessage();
    mc.moveMetadata(metadataCollection, jmsMsg);
    assertEquals(getStringValue(), jmsMsg.getStringProperty(HEADER));
    assertNull(jmsMsg.getStringProperty("JMSCorrelationID"));
    assertValue(jmsMsg);
  }

  abstract MetadataConverter createConverter();

  abstract String getStringValue();

  abstract void assertValue(Message jmsMsg) throws JMSException;

}
