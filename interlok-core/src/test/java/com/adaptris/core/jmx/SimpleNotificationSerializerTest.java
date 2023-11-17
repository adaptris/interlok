package com.adaptris.core.jmx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;
import java.util.Properties;

import javax.management.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class SimpleNotificationSerializerTest {


  
  

  @Test
  public void testSerialize(TestInfo info) throws Exception {
    String myName = info.getDisplayName(); 
    Notification n = new Notification(myName, myName, 1, myName);
    n.setUserData(new Object());
    SimpleNotificationSerializer serializer = new SimpleNotificationSerializer();
    AdaptrisMessage msg = serializer.serialize(n, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    try (InputStream in = msg.getInputStream()) {
      Properties p = new Properties();
      p.load(in);
      assertEquals(myName, p.getProperty("Source"));
      assertEquals(myName, p.getProperty("Type"));
      assertEquals(myName, p.getProperty("Message"));
      assertEquals("1", p.getProperty("SequenceNumber"));
      assertNotNull(p.getProperty("Timestamp"));
      assertNotNull(msg.getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    }
  }

  @Test
  public void testSerialize_NoUserData(TestInfo info) throws Exception {
    String myName = info.getDisplayName();
    Notification n = new Notification(myName, myName, 1, myName);
    SimpleNotificationSerializer serializer = new SimpleNotificationSerializer();
    AdaptrisMessage msg = serializer.serialize(n, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    try (InputStream in = msg.getInputStream()) {
      Properties p = new Properties();
      p.load(in);
      assertEquals(myName, p.getProperty("Source"));
      assertEquals(myName, p.getProperty("Type"));
      assertEquals(myName, p.getProperty("Message"));
      assertEquals("1", p.getProperty("SequenceNumber"));
      assertNotNull(p.getProperty("Timestamp"));
      assertNull(msg.getObjectHeaders().get(NotificationSerializer.OBJ_METADATA_USERDATA));
    }
  }
}
