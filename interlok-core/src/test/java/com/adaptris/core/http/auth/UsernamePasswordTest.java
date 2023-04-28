package com.adaptris.core.http.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.PasswordAuthentication;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;

public class UsernamePasswordTest {

  @Test
  public void testConfigured_GetPasswordAuthentication() throws Exception {
    ConfiguredUsernamePassword pw = new ConfiguredUsernamePassword("username", "password");
    PasswordAuthentication auth = pw
        .getPasswordAuthentication(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    assertEquals("username", auth.getUserName());
    assertEquals("password", String.copyValueOf(auth.getPassword()));
  }

  @Test
  public void testBadPassword() throws Exception {
    Assertions.assertThrows(CoreException.class, () -> {
      ConfiguredUsernamePassword pw = new ConfiguredUsernamePassword("username", "PW:password");
      pw.getPasswordAuthentication(AdaptrisMessageFactory.getDefaultInstance().newMessage());
    });
  }

  @Test
  public void testMetadata_GetPasswordAuthentication() throws Exception {
    MetadataUsernamePassword pw = new MetadataUsernamePassword("userKey", "passwordKey");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMessageHeader("userKey", "username");
    msg.addMessageHeader("passwordKey", "password");
    PasswordAuthentication auth = pw.getPasswordAuthentication(msg);
    assertEquals("username", auth.getUserName());
    assertEquals("password", String.copyValueOf(auth.getPassword()));
  }

}
