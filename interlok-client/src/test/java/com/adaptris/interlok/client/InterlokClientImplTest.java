package com.adaptris.interlok.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.SerializableMessage;

public class InterlokClientImplTest {

  @BeforeEach
  public void setUp() throws Exception {}

  @AfterEach
  public void tearDown() throws Exception {}

  @Test
  public void testPublish(TestInfo info) throws Exception {
    MessageTarget target =
        new MessageTarget().withAdapter(info.getDisplayName()).withChannel(info.getDisplayName())
            .withWorkflow(info.getDisplayName());

    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(info.getDisplayName(), info.getDisplayName());

    InterlokClientStub c = new InterlokClientStub();
    c.processAsync(target, info.getDisplayName(), hdrs);
    assertNotNull(c.msg);
    assertEquals(target, c.target);
    assertNotNull(c.msg.getUniqueId());
    assertEquals(info.getDisplayName(), c.msg.getContent());
    assertNull(c.msg.getContentEncoding());
    assertEquals(1, c.msg.getMessageHeaders().size());
    assertEquals(info.getDisplayName(), c.msg.getMessageHeaders().get(info.getDisplayName()));

  }


  private class InterlokClientStub extends InterlokClientImpl {

    private MessageTarget target;
    private SerializableMessage msg;

    @Override
    public void processAsync(MessageTarget f, SerializableMessage m) throws InterlokException {
      target = f;
      msg = m;
    }

    @Override
    public SerializableMessage process(MessageTarget f, SerializableMessage m) throws InterlokException {
      return m;
    }

    @Override
    public void connect() throws InterlokException {
    }

    @Override
    public void disconnect() throws InterlokException {
    }

  }
}
