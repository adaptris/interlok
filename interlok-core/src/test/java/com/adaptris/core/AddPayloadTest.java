package com.adaptris.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.adaptris.core.common.ConstantDataInputParameter;

public class AddPayloadTest extends ServiceCase {
  private final MultiPayloadMessageFactory messageFactory = new MultiPayloadMessageFactory();

  private static final String ENCODING = "UTF-8";

  private static final String[] CONTENT = {
      "Bacon ipsum dolor amet bresaola ball tip flank, doner pork chop ham hock rump kielbasa pork loin beef burgdoggen short ribs tongue.",
      "Bacon ipsum dolor amet ball tip venison pastrami, short loin kielbasa andouille rump chuck pork spare ribs salami turducken shankle.",
      "Cupcake ipsum dolor sit amet dragee carrot cake halvah jujubes.",
      "Cupcake ipsum dolor sit. Amet jelly cupcake sweet roll I love lollipop." };

  private static final String[] ID = { "bacon-1", "bacon-2", "cupcake-1", "cupcake-2" };

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
  public void testService() throws Exception {
    MultiPayloadAdaptrisMessage message = getMessage(ID[0], CONTENT[0], ENCODING);

    assertEquals(ID[0], message.getCurrentPayloadId());
    assertEquals(CONTENT[0], message.getContent());

    for (int i = 1; i < ID.length; i++) {
      assertEquals(i, message.getPayloadCount());
      AddPayloadService service = getService(ID[i], CONTENT[i], ENCODING);
      assertEquals(ID[i], service.getNewPayloadId());
      assertEquals(ENCODING, service.getNewPayloadEncoding());
      service.doService(message);
    }

    for (int i = 0; i < ID.length; i++) {
      assertEquals(CONTENT[i].getBytes("UTF-8").length, message.getSize(ID[i]));
      assertEquals(CONTENT[i], message.getContent(ID[i]));
    }

    message.deletePayload(ID[3]);
    assertEquals(3, message.getPayloadCount());
  }

  @Test
  public void testServiceNoEncoding() throws Exception {
    MultiPayloadAdaptrisMessage message = getMessage(ID[0], CONTENT[0], ENCODING);

    assertEquals(ID[0], message.getCurrentPayloadId());
    assertEquals(CONTENT[0], message.getContent());

    AddPayloadService service = getService(ID[1], CONTENT[1], null);
    assertEquals(ID[1], service.getNewPayloadId());
    assertNull(service.getNewPayloadEncoding());
    service.doService(message);
  }

  @Test
  public void testServiceNoPayload() throws Exception {
    try {
      MultiPayloadAdaptrisMessage message = getMessage(ID[0], CONTENT[0], ENCODING);

      assertEquals(ID[0], message.getCurrentPayloadId());
      assertEquals(CONTENT[0], message.getContent());

      AddPayloadService service = getService(ID[1], null, null);
      service.setNewPayload(null);
      assertEquals(ID[1], service.getNewPayloadId());
      service.doService(message);
      fail();
    } catch (ServiceException e) {
      /* expected */
    }
  }

  @Test
  public void testWrongMessageType() {
    try {
      Service service = getService(ID[0], null, null);
      AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
      service.doService(message);
      fail();
    } catch (ServiceException e) {
      /* expected */
    }
  }

  private MultiPayloadAdaptrisMessage getMessage(String id, String payload, String encoding) {
    return (MultiPayloadAdaptrisMessage) messageFactory.newMessage(id, payload, encoding);
  }

  private AddPayloadService getService(String id, String payload, String encoding) {
    AddPayloadService service = new AddPayloadService();
    service.setNewPayloadId(id);
    service.setNewPayload(new ConstantDataInputParameter(payload));
    service.setNewPayloadEncoding(encoding);
    return service;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return getService(ID[0], CONTENT[0], ENCODING);
  }
}
