package com.adaptris.core.http.jetty.retry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import java.util.Collections;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;

public class RetryStoreTest implements RetryStore {

  @Before
  public void setUp() throws Exception {
    LifecycleHelper.initAndStart(this, false);
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(this, false);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDefaultDelete() throws Exception {
    delete("");
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
  public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata,
      AdaptrisMessageFactory factory) throws InterlokException {
    return null;
  }

  @Override
  public Map<String, String> getMetadata(String msgId) throws InterlokException {
    return Collections.EMPTY_MAP;
  }

}
