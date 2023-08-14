package com.adaptris.core.http.jetty.retry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;

public class RetryStoreTest implements RetryStore {

  @BeforeEach
  public void setUp() throws Exception {
    LifecycleHelper.initAndStart(this, false);
  }

  @AfterEach
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(this, false);

  }

  @Test
  public void testDefaultDelete() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      delete("");
    });
  }

  @Test
  public void testDefaultBuild() throws Exception {
    assertNull(buildForRetry(""));
  }

  @Test
  public void testDefaultReport() throws Exception {
    assertFalse(report().iterator().hasNext());
  }

  @Override
  public void write(AdaptrisMessage msg) throws InterlokException {
    throw new UnsupportedOperationException();
  }

  @Override
  public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata, AdaptrisMessageFactory factory)
      throws InterlokException {
    return null;
  }

  @Override
  public Map<String, String> getMetadata(String msgId) throws InterlokException {
    return Collections.EMPTY_MAP;
  }

  @Override
  public void acknowledge(String acknowledgeId) throws InterlokException {
   // null implementation
  }

  @Override
  public void deleteAcknowledged() throws InterlokException {
   // null implementation   
  }
 
  @Override
  public void updateRetryCount(String messageId) throws InterlokException {
 // null implementation
  }

  @Override
  public void makeConnection(AdaptrisConnection connection) {
   // null implementation
  }

}
