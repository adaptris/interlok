package com.adaptris.core.common;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MultiPayloadAdaptrisMessage;
import com.adaptris.core.MultiPayloadMessageFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class MultiPayloadByteArrayOutputParameterTest
{
  private static final String ID = "bacon";
  private static final byte[] PAYLOAD = "Bacon ipsum dolor amet short loin porchetta ham turducken chicken tail meatball frankfurter. Rump t-bone flank kielbasa ribeye strip steak landjaeger fatback capicola pastrami cow brisket leberkas jerky. Ham pork chop chislic ground round prosciutto. Sirloin porchetta ribeye, spare ribs strip steak fatback cupim short loin burgdoggen landjaeger. Ground round tri-tip sirloin pig jowl shoulder pork loin cow jerky picanha pastrami. Pork rump bacon strip steak pig bresaola kielbasa ball tip tongue drumstick t-bone. Boudin kevin filet mignon prosciutto tongue short loin spare ribs.".getBytes();

  @Rule
  public TestName testName = new TestName();

  @Test
  public void testInsert() throws Exception
  {
    MultiPayloadByteArrayOutputParameter parameter = new MultiPayloadByteArrayOutputParameter();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    parameter.insert(PAYLOAD, ID, message);
    assertArrayEquals(PAYLOAD, message.getPayload(ID));
  }

  @Test
  public void testInsertSetId() throws Exception
  {
    MultiPayloadByteArrayOutputParameter parameter = new MultiPayloadByteArrayOutputParameter();
    parameter.setPayloadId(ID);
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)new MultiPayloadMessageFactory().newMessage();
    parameter.insert(PAYLOAD, message);
    assertArrayEquals(PAYLOAD, message.getPayload(ID));
  }

  @Test
  public void testInsertUseDefaultId() throws Exception
  {
    MultiPayloadByteArrayOutputParameter parameter = new MultiPayloadByteArrayOutputParameter();
    MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();
    MultiPayloadAdaptrisMessage message = (MultiPayloadAdaptrisMessage)messageFactory.newMessage();
    parameter.insert(PAYLOAD, null, message);
    assertArrayEquals(PAYLOAD, message.getPayload(messageFactory.getDefaultPayloadId()));
  }

  @Test
  public void testWrongMessageType()
  {
    try
    {
      MultiPayloadByteArrayOutputParameter parameter = new MultiPayloadByteArrayOutputParameter();
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
      parameter.insert(PAYLOAD, message);
      fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
