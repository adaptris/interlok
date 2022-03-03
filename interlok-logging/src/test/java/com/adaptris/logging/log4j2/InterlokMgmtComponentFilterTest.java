package com.adaptris.logging.log4j2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.junit.Test;

public class InterlokMgmtComponentFilterTest {

  private static InterlokMgmtComponentFilter createFilter() {
    InterlokMgmtComponentFilter.Builder builder = InterlokMgmtComponentFilter.newBuilder();
    builder.setOnMatch(Result.DENY);
    builder.setOnMismatch(Result.ACCEPT);
    return builder.build();
  }

  @Test
  public void testBuilder() throws Exception {
    InterlokMgmtComponentFilter filter = createFilter();
    assertNotNull(filter);
  }

  @Test
  public void testFilterLogEvent_Mismatch() throws Exception {
    InterlokMgmtComponentFilter filter = createFilter();
    LogEvent event = new Log4jLogEvent.Builder().build();
    assertEquals(Result.ACCEPT, filter.filter(event));
  }

  @Test
  public void testFilterLogEvent_Match() throws Exception {
    InterlokMgmtComponentFilter filter = createFilter();
    SortedArrayStringMap map = new SortedArrayStringMap();
    map.putValue(InterlokMgmtComponentFilter.CONTEXT_KEY, "hello");
    LogEvent event = new Log4jLogEvent.Builder().setContextData(map).build();
    assertEquals(Result.DENY, filter.filter(event));
  }

  @Test
  public void testFilter() throws Exception {
    InterlokMgmtComponentFilter filter = createFilter();
    assertEquals(Result.ACCEPT,
        filter.filter(null, null, null, (Message) null, null));
    assertEquals(Result.ACCEPT,
        filter.filter(null, null, null, null));
    assertEquals(Result.ACCEPT,
        filter.filter(null, null, null, (Object) null, null));
  }
}

