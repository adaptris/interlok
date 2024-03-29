package com.adaptris.core.transform.schema;

import static com.adaptris.core.transform.schema.CollectingErrorHandlerTest.createException;
import static com.adaptris.core.transform.schema.ViolationHandlerImpl.DEFAULT_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.XStreamMarshaller;

public class ViolationHandlerTest {

  @Test
  public void testAsMetadata() throws Exception {
    ViolationsAsMetadata handler = new ViolationsAsMetadata().withMetadataKey(DEFAULT_KEY);
    List<SAXParseException> exceptions = Arrays.asList(createException("first"), createException("second"));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    handler.handle(exceptions, msg);
    assertTrue(msg.headersContainsKey(DEFAULT_KEY));
    SchemaViolations v = (SchemaViolations) new XStreamMarshaller().unmarshal(msg.getMetadataValue(DEFAULT_KEY));
    assertEquals(2, v.getViolations().size());
  }

  @Test
  public void testAsMetadata_WithException() throws Exception {
    Assertions.assertThrows(ServiceException.class, () -> {
      ViolationsAsMetadata handler = new ViolationsAsMetadata().withMetadataKey(DEFAULT_KEY);
      List<SAXParseException> exceptions = Arrays.asList(createException("first"), createException("second"));
      AdaptrisMessage msg = mock(AdaptrisMessage.class);
      doThrow(new RuntimeException()).when(msg).addMessageHeader(anyString(), anyString());
      doThrow(new RuntimeException()).when(msg).addMetadata(anyString(), anyString());
      doThrow(new RuntimeException()).when(msg).addMetadata(any(MetadataElement.class));
      doThrow(new RuntimeException()).when(msg).addMetadata(any(Set.class));
      handler.handle(exceptions, msg);
    });
  }

  @Test
  public void testAsObjectMetadata() throws Exception {
    ViolationsAsObjectMetadata handler = new ViolationsAsObjectMetadata().withObjectMetadataKey(DEFAULT_KEY);
    List<SAXParseException> exceptions = Arrays.asList(createException("first"), createException("second"));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    handler.handle(exceptions, msg);
    assertTrue(msg.getObjectHeaders().containsKey(DEFAULT_KEY));
    SchemaViolations v = (SchemaViolations) msg.getObjectHeaders().get(DEFAULT_KEY);
    assertEquals(2, v.getViolations().size());
  }

  @Test
  public void testAsPayload() throws Exception {
    OverwritePayload handler = new OverwritePayload();
    List<SAXParseException> exceptions = Arrays.asList(createException("first"), createException("second"));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    handler.handle(exceptions, msg);
    assertFalse(msg.headersContainsKey(DEFAULT_KEY));
    SchemaViolations v = (SchemaViolations) new XStreamMarshaller().unmarshal(msg.getContent());
    assertEquals(2, v.getViolations().size());
  }

  @Test
  public void testAsPayload_WithException() throws Exception {
    Assertions.assertThrows(ServiceException.class, () -> {
      OverwritePayload handler = new OverwritePayload();
      List<SAXParseException> exceptions = Arrays.asList(createException("first"), createException("second"));
      AdaptrisMessage msg = mock(AdaptrisMessage.class);
      doThrow(new RuntimeException()).when(msg).setContent(anyString(), anyString());
      handler.handle(exceptions, msg);
    });
  }

}
