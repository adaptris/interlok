package com.adaptris.interlok.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class MessageTargetTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testAdapter(TestInfo info) {
    MessageTarget t = new MessageTarget();
    assertNull(t.getAdapter());
    t.setAdapter(info.getDisplayName());
    assertEquals(info.getDisplayName(), t.getAdapter());
  }

  @Test
  public void testChannel(TestInfo info) {
    MessageTarget t = new MessageTarget();
    assertNull(t.getChannel());
    t.setChannel(info.getDisplayName());
    assertEquals(info.getDisplayName(), t.getChannel());
  }


  @Test
  public void testWorkflow(TestInfo info) {
    MessageTarget t = new MessageTarget();
    assertNull(t.getWorkflow());
    t.setWorkflow(info.getDisplayName());
    assertEquals(info.getDisplayName(), t.getWorkflow());
  }

  @Test
  public void testConvenience(TestInfo info) {
    MessageTarget t =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());
    assertEquals(info.getDisplayName(), t.getAdapter());
    assertEquals(info.getDisplayName(), t.getChannel());
    assertEquals(info.getDisplayName(), t.getWorkflow());
  }


}
