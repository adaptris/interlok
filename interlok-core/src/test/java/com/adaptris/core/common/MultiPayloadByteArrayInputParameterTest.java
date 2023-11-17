package com.adaptris.core.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;

public class MultiPayloadByteArrayInputParameterTest
{
  private static final String ID = "bacon";
  private static final byte[] PAYLOAD = "Bacon ipsum dolor amet short loin porchetta ham turducken chicken tail meatball frankfurter. Rump t-bone flank kielbasa ribeye strip steak landjaeger fatback capicola pastrami cow brisket leberkas jerky. Ham pork chop chislic ground round prosciutto. Sirloin porchetta ribeye, spare ribs strip steak fatback cupim short loin burgdoggen landjaeger. Ground round tri-tip sirloin pig jowl shoulder pork loin cow jerky picanha pastrami. Pork rump bacon strip steak pig bresaola kielbasa ball tip tongue drumstick t-bone. Boudin kevin filet mignon prosciutto tongue short loin spare ribs.".getBytes();

  @Test
  public void testExtract() throws Exception
  {
    MultiPayloadByteArrayInputParameter parameter = new MultiPayloadByteArrayInputParameter();
    parameter.setPayloadId(ID);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage(ID, PAYLOAD);
    assertArrayEquals(PAYLOAD, parameter.extract(message));
    assertArrayEquals(PAYLOAD, parameter.extract(ID, message));
    assertArrayEquals(PAYLOAD, parameter.extract(null, message));
  }

  @Test
  public void testWrongMessageType()
  {
    try
    {
      MultiPayloadByteArrayInputParameter parameter = new MultiPayloadByteArrayInputParameter();
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
