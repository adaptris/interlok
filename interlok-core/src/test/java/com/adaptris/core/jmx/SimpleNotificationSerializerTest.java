package com.adaptris.core.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Properties;

import javax.management.Notification;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public class SimpleNotificationSerializerTest {


  @Rule
  public TestName testName = new TestName();

  @Test
  public void testSerialize() throws Exception {
    String myName = testName.getMethodName(); 
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
  public void testSerialize_NoUserData() throws Exception {
    String myName = testName.getMethodName();
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
