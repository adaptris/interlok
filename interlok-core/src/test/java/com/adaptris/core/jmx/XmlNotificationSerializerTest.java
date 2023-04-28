package com.adaptris.core.jmx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.management.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.XmlUtils;

public class XmlNotificationSerializerTest {


  
  

  @Test
  public void testSerialize(TestInfo info) throws Exception {
    String myName = info.getDisplayName(); 
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
  public void testSerialize_NoUserData(TestInfo info) throws Exception {
    String myName = info.getDisplayName();
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
  public void testSerialize_WithContentEncoding(TestInfo info) throws Exception {
    String myName = info.getDisplayName();
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
  public void testSerialize_WithContentEncoding_FromMessage(TestInfo info) throws Exception {
    String myName = info.getDisplayName();
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
