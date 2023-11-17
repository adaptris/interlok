package com.adaptris.core.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;

public class ByteArrayPayloadDataOutputParameterTest
{
  private static final byte[] PAYLOAD = "Bacon ipsum dolor amet short loin porchetta ham turducken chicken tail meatball frankfurter. Rump t-bone flank kielbasa ribeye strip steak landjaeger fatback capicola pastrami cow brisket leberkas jerky. Ham pork chop chislic ground round prosciutto. Sirloin porchetta ribeye, spare ribs strip steak fatback cupim short loin burgdoggen landjaeger. Ground round tri-tip sirloin pig jowl shoulder pork loin cow jerky picanha pastrami. Pork rump bacon strip steak pig bresaola kielbasa ball tip tongue drumstick t-bone. Boudin kevin filet mignon prosciutto tongue short loin spare ribs.".getBytes();

  @Test
  public void testInsert() throws Exception
  {
    ByteArrayPayloadDataOutputParameter parameter = new ByteArrayPayloadDataOutputParameter();
    AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
    parameter.insert(PAYLOAD, message);
    assertArrayEquals(PAYLOAD, message.getPayload());
  }

  @Test
  public void testException() throws Exception
  {
    try {
      ByteArrayPayloadDataOutputParameter parameter = new ByteArrayPayloadDataOutputParameter();
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
      parameter.insert(null, message);
      fail();
    } catch (Exception e) {
      // expected
    }
  }
}
