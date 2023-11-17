package com.adaptris.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;

public class MultiPayloadStreamInputParameterTest
{
  private static final String ID = "cupcake";
  private static final byte[] PAYLOAD = "Cupcake ipsum dolor sit amet donut topping brownie wafer. Pie dessert tiramisu. Toffee candy canes fruitcake. Pastry cookie jelly-o tiramisu I love carrot cake lollipop cake halvah. Icing tart jelly ice cream. I love muffin chocolate cake sweet roll I love. Apple pie souffle I love I love pie cake. Carrot cake jelly beans cake.".getBytes();

  @Test
  public void testExtract() throws Exception
  {
    MultiPayloadStreamInputParameter parameter = new MultiPayloadStreamInputParameter();
    parameter.setPayloadId(ID);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(ID, PAYLOAD);
    try (InputStream stream = parameter.extract(message))
    {
      for (byte b : PAYLOAD)
      {
        assertEquals(b, stream.read());
      }
    }
    try (InputStream stream = parameter.extract(ID, message))
    {
      for (byte b : PAYLOAD)
      {
        assertEquals(b, stream.read());
      }
    }
    try (InputStream stream = parameter.extract(null, message))
    {
      for (byte b : PAYLOAD)
      {
        assertEquals(b, stream.read());
      }
    }
  }

  @Test
  public void testWrongMessageType()
  {
    try
    {
      MultiPayloadStreamInputParameter parameter = new MultiPayloadStreamInputParameter();
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
