package com.adaptris.core.http.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.text.DateFormatUtil;

public class MetadataTokenWriterTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testApply() throws Exception {
    AccessToken token = new AccessToken("Bearer", "token").withRefreshToken("refresh")
        .withExpiry(DateFormatUtil.format(new Date(System.currentTimeMillis() + 2000)));
    MetadataAccessTokenWriter writer = new MetadataAccessTokenWriter().withTokenKey("access_token")
        .withTokenExpiryKey("expiry_token")
        .withRefreshTokenKey("refresh_token");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      LifecycleHelper.initAndStart(writer);
      writer.apply(token, msg);
      assertTrue(msg.headersContainsKey("access_token"));
      assertEquals("Bearer token", msg.getMetadataValue("access_token"));
      assertTrue(msg.headersContainsKey("expiry_token"));
      assertTrue(msg.headersContainsKey("refresh_token"));
    } finally {
      LifecycleHelper.stopAndClose(writer);
    }
  }

  @Test
  public void testApply_NoAdditionalKeys() throws Exception {
    AccessToken token = new AccessToken("Bearer", "token").withRefreshToken("refresh")
        .withExpiry(DateFormatUtil.format(new Date(System.currentTimeMillis() + 2000)));
    MetadataAccessTokenWriter writer = new MetadataAccessTokenWriter().withTokenKey("access_token");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      LifecycleHelper.initAndStart(writer);
      writer.apply(token, msg);
      assertTrue(msg.headersContainsKey("access_token"));
      assertEquals("Bearer token", msg.getMetadataValue("access_token"));
      assertFalse(msg.headersContainsKey("expiry_token"));
      assertFalse(msg.headersContainsKey("refresh_token"));
    } finally {
      LifecycleHelper.stopAndClose(writer);
    }
  }

  @Test
  public void testApply_NoAdditionalData() throws Exception {
    AccessToken token = new AccessToken("Bearer", "token");
    MetadataAccessTokenWriter writer = new MetadataAccessTokenWriter().withTokenKey("access_token")
        .withTokenExpiryKey("expiry_token").withRefreshTokenKey("refresh_token");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    try {
      LifecycleHelper.initAndStart(writer);
      writer.apply(token, msg);
      assertTrue(msg.headersContainsKey("access_token"));
      assertEquals("Bearer token", msg.getMetadataValue("access_token"));
      assertFalse(msg.headersContainsKey("expiry_token"));
      assertFalse(msg.headersContainsKey("refresh_token"));
    } finally {
      LifecycleHelper.stopAndClose(writer);
    }
  }
}
