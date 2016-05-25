package com.adaptris.core.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.management.Notification;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

public class XmlNotificationSerializerTest {


  @Rule
  public TestName testName = new TestName();

  @Test
  public void testSerialize() throws Exception {
    String myName = testName.getMethodName(); 
    Notification n = new Notification(myName, myName, 1, myName);
    n.setUserData(new Object());
    XmlNotificationSerializer serializer = new XmlNotificationSerializer();
    AdaptrisMessage msg = serializer.serialize(n, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    XmlUtils utils = XmlHelper.createXmlUtils(msg, null, null);
    assertNotNull(msg.getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Message"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Type"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Source"));
    assertEquals("1", utils.getSingleTextItem("/Notification/SequenceNumber"));
    assertNotNull(utils.getSingleTextItem("/Notification/Timestamp"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_NoUserData() throws Exception {
    String myName = testName.getMethodName();
    Notification n = new Notification(myName, myName, 1, myName);
    XmlNotificationSerializer serializer = new XmlNotificationSerializer();
    AdaptrisMessage msg = serializer.serialize(n, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    XmlUtils utils = XmlHelper.createXmlUtils(msg, null, null);
    assertNull(msg.getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Message"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Type"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Source"));
    assertEquals("1", utils.getSingleTextItem("/Notification/SequenceNumber"));
    assertNotNull(utils.getSingleTextItem("/Notification/Timestamp"));
    assertEquals("UTF-8", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_WithContentEncoding() throws Exception {
    String myName = testName.getMethodName();
    Notification n = new Notification(myName, myName, 1, myName);
    n.setUserData(new Object());
    XmlNotificationSerializer serializer = new XmlNotificationSerializer();
    serializer.setOutputMessageEncoding("ISO-8859-1");
    AdaptrisMessage msg = serializer.serialize(n, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    XmlUtils utils = XmlHelper.createXmlUtils(msg, null, null);
    assertNotNull(msg.getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Message"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Type"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Source"));
    assertEquals("1", utils.getSingleTextItem("/Notification/SequenceNumber"));
    assertNotNull(utils.getSingleTextItem("/Notification/Timestamp"));
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }

  @Test
  public void testSerialize_WithContentEncoding_FromMessage() throws Exception {
    String myName = testName.getMethodName();
    Notification n = new Notification(myName, myName, 1, myName);
    n.setUserData(new Object());
    XmlNotificationSerializer serializer = new XmlNotificationSerializer();
    AdaptrisMessageFactory factory = new DefaultMessageFactory();
    factory.setDefaultCharEncoding("ISO-8859-1");
    AdaptrisMessage msg = serializer.serialize(n, factory.newMessage());
    XmlUtils utils = XmlHelper.createXmlUtils(msg, null, null);
    assertNotNull(msg.getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Message"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Type"));
    assertEquals(myName, utils.getSingleTextItem("/Notification/Source"));
    assertEquals("1", utils.getSingleTextItem("/Notification/SequenceNumber"));
    assertNotNull(utils.getSingleTextItem("/Notification/Timestamp"));
    assertEquals("ISO-8859-1", msg.getContentEncoding());
  }
}
