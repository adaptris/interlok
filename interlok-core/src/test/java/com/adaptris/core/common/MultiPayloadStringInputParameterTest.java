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

public class MultiPayloadStringInputParameterTest
{
  private static final String ID = "hipster";
  private static final String PAYLOAD = "Hipster ipsum dolor amet quinoa XOXO literally ramps la croix four loko messenger bag. Neutra leggings occupy mlkshk. Sriracha drinking vinegar you probably haven't heard of them cliche. Woke tousled kinfolk jean shorts hexagon mustache cold-pressed blue bottle raclette try-hard. Art party cloud bread hell of, neutra blue bottle flexitarian cliche kombucha blog.";
  private static final String ENCODING = "UTF-8";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testExtract() throws Exception
  {
    MultiPayloadStringInputParameter parameter = new MultiPayloadStringInputParameter();
    parameter.setPayloadId(ID);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(ID, PAYLOAD, ENCODING);
    assertEquals(PAYLOAD, parameter.extract(message));
    assertEquals(PAYLOAD, parameter.extract(ID, message));
    assertEquals(PAYLOAD, parameter.extract(null, message));
  }

  @Test
  public void testWrongMessageType()
  {
    try
    {
      MultiPayloadStringInputParameter parameter = new MultiPayloadStringInputParameter();
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(PAYLOAD);
      parameter.extract(message);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
