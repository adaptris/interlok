package com.adaptris.interlok.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.SerializableMessage;

public class InterlokClientImplTest {

  @Rule
  public TestName testName = new TestName();

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testPublish() throws Exception {
    MessageTarget target =
        new MessageTarget().withAdapter(testName.getMethodName()).withChannel(testName.getMethodName())
            .withWorkflow(testName.getMethodName());

    Map<String, String> hdrs = new HashMap<>();
    hdrs.put(testName.getMethodName(), testName.getMethodName());

    InterlokClientStub c = new InterlokClientStub();
    c.processAsync(target, testName.getMethodName(), hdrs);
    assertNotNull(c.msg);
    assertEquals(target, c.target);
    assertNotNull(c.msg.getUniqueId());
    assertEquals(testName.getMethodName(), c.msg.getContent());
    assertNull(c.msg.getContentEncoding());
    assertEquals(1, c.msg.getMessageHeaders().size());
    assertEquals(testName.getMethodName(), c.msg.getMessageHeaders().get(testName.getMethodName()));

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
