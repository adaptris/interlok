package com.adaptris.core.common;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MultiPayloadStreamOutputParameterTest
{
  private static final String ID = "cupcake";
  private static final String PAYLOAD = "Cupcake ipsum dolor sit amet donut topping brownie wafer. Pie dessert tiramisu. Toffee candy canes fruitcake. Pastry cookie jelly-o tiramisu I love carrot cake lollipop cake halvah. Icing tart jelly ice cream. I love muffin chocolate cake sweet roll I love. Apple pie souffle I love I love pie cake. Carrot cake jelly beans cake.";
  private static final String ENCODING = "UTF-8";

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testInsert() throws Exception
  {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    try (InputStream in = new ByteArrayInputStream(PAYLOAD.getBytes()))
    {
      InputStreamWithEncoding stream = new InputStreamWithEncoding(in, ENCODING);
      MultiPayloadStreamOutputParameter parameter = new MultiPayloadStreamOutputParameter();
      parameter.insert(stream, ID, message);
    }
    assertEquals(PAYLOAD, message.getContent(ID));
  }

  @Test
  public void testInsertSetId() throws Exception
  {
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    try (InputStream in = new ByteArrayInputStream(PAYLOAD.getBytes()))
    {
      InputStreamWithEncoding stream = new InputStreamWithEncoding(in, ENCODING);
      MultiPayloadStreamOutputParameter parameter = new MultiPayloadStreamOutputParameter();
      parameter.setPayloadId(ID);
      parameter.insert(stream, message);
    }
    assertEquals(PAYLOAD, message.getContent(ID));
  }

  @Test
  public void testInsertUseDefaultId() throws Exception
  {
    MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage();
    try (InputStream in = new ByteArrayInputStream(PAYLOAD.getBytes()))
    {
      InputStreamWithEncoding stream = new InputStreamWithEncoding(in, ENCODING);
      MultiPayloadStreamOutputParameter parameter = new MultiPayloadStreamOutputParameter();
      parameter.insert(stream, null, message);
    }
    assertEquals(PAYLOAD, message.getContent(messageFactory.getDefaultPayloadId()));
  }

  @Test
  public void testInsertNoEncoding() throws Exception
  {
    MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage();
    try (InputStream in = new ByteArrayInputStream(PAYLOAD.getBytes()))
    {
      InputStreamWithEncoding stream = new InputStreamWithEncoding(in, null);
      MultiPayloadStreamOutputParameter parameter = new MultiPayloadStreamOutputParameter();
      parameter.setContentEncoding(null);
      parameter.insert(stream, ID, message);
    }
    assertEquals(PAYLOAD, message.getContent(ID));
  }

  @Test
  public void testInsertNullStream() throws Exception
  {
    MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage();
    try
    {
      InputStreamWithEncoding stream = new InputStreamWithEncoding(null, ENCODING);
      MultiPayloadStreamOutputParameter parameter = new MultiPayloadStreamOutputParameter();
      parameter.insert(stream, ID, message);
    }
    catch (Exception e)
    {
      // expected
    }
  }

  @Test
  public void testWrongMessageType()
  {
    try
    {
      MultiPayloadStreamOutputParameter parameter = new MultiPayloadStreamOutputParameter();
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
      parameter.insert(null, message);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
