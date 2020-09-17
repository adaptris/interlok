package com.adaptris.core;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.stubs.StubEventHandler;

public class HeartbeatTimerTaskTest {
  
  private HeartbeatTimerTask timerTask; 
  
  private Adapter mockAdapter;
  
  private MockEventHandler mockEventHandler;
  
  @Before
  public void setUp() throws Exception {    
    mockEventHandler = new MockEventHandler();
    
    mockAdapter = new Adapter();
    mockAdapter.setEventHandler(mockEventHandler);
    
    timerTask = new HeartbeatTimerTask(HeartbeatEvent.class, mockAdapter);
  }
  
  @After
  public void tearDown() throws Exception {
    
  }

  @Test
  public void testRunEventLogger() throws Exception {
    timerTask.run();
    
    await()
    .atMost(Duration.ofSeconds(1))
  .with()
    .pollInterval(Duration.ofMillis(100))
    .until(mockEventHandler::getEventCount, greaterThanOrEqualTo(1));
    
    assertEquals(1, mockEventHandler.getEventCount());
  }
  
  class MockEventHandler extends StubEventHandler {
    int eventCount;
    
    @Override
    public void send(Event evt) throws CoreException {
      eventCount ++;
    }
    
    int getEventCount() {
      return eventCount;
    }
  }
}
