package com.adaptris.core.common;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultiPayloadStringOutputParameterTest
{
  private static final String ID = "hipster";
  private static final String PAYLOAD = "Hipster ipsum dolor amet quinoa XOXO literally ramps la croix four loko messenger bag. Neutra leggings occupy mlkshk. Sriracha drinking vinegar you probably haven't heard of them cliche. Woke tousled kinfolk jean shorts hexagon mustache cold-pressed blue bottle raclette try-hard. Art party cloud bread hell of, neutra blue bottle flexitarian cliche kombucha blog.";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testInsert() throws Exception
  {
    MultiPayloadStringOutputParameter parameter = new MultiPayloadStringOutputParameter();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    parameter.insert(PAYLOAD, ID, message);
    assertEquals(PAYLOAD, message.getContent(ID));
  }

  @Test
  public void testInsertSetId() throws Exception
  {
    MultiPayloadStringOutputParameter parameter = new MultiPayloadStringOutputParameter();
    parameter.setPayloadId(ID);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    parameter.insert(PAYLOAD, message);
    assertEquals(PAYLOAD, message.getContent(ID));
  }

  @Test
  public void testInsertUseDefaultId() throws Exception
  {
    MultiPayloadStringOutputParameter parameter = new MultiPayloadStringOutputParameter();
    MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage();
    parameter.insert(PAYLOAD, null, message);
    assertEquals(PAYLOAD, message.getContent(messageFactory.getDefaultPayloadId()));
  }

  @Test
  public void testWrongMessageType()
  {
    try
    {
      MultiPayloadStringOutputParameter parameter = new MultiPayloadStringOutputParameter();
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      parameter.insert(PAYLOAD, message);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
