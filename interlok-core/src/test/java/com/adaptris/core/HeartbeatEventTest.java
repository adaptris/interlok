package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import com.adaptris.interlok.util.Closer;

public class HeartbeatEventTest {

  private HeartbeatEvent event;

  @Mock private Logger mockLogger;

  private AutoCloseable openMocks;

  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    event = new HeartbeatEvent();
    event.setAdapterStateSummary(null);
    event.setHeartbeatTime(new Date().getTime());
  }

  @After
  public void tearDown() throws Exception {
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testExtractState() throws Exception {
    Adapter adapter = new Adapter();
    adapter.setUniqueId("MyAdapter");
    Channel channel = new Channel();
    channel.setUniqueId("MyChannel");
    adapter.getChannelList().add(channel);

    event.extractState(adapter);

    assertEquals("MyAdapter", event.getAdapterStateSummary().getAdapterState().getKey());
  }

  @Test
  public void testLogEvent() throws Exception {
    event.logEvent(mockLogger);

    verify(mockLogger).trace(anyString(), anyString());
  }

}
