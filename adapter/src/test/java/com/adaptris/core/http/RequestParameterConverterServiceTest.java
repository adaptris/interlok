/*
 * $RCSfile: RequestParameterConverterServiceTest.java,v $
 * $Revision: 1.5 $
 * $Date: 2008/08/13 13:28:44 $
 * $Author: lchan $
 */
package com.adaptris.core.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.http.Http;

public class RequestParameterConverterServiceTest extends HttpServiceExample {

  private static final String TEST_VALUE = "the quick brown fox jumps over the lazy dog";
  private static final String XML_VALUE = "<?xml version=\"1.0\" "
      + "encoding=\"UTF-8\"?>" + System.getProperty("line.separator")
      + "<root>" + "<unencrypted>Unencrypted xpath</unencrypted>" + "</root>";

  private static final String SAVE_PARAM = "PostParameterToSaveAsPayload";
  private RequestParameterConverterService service;

  public RequestParameterConverterServiceTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    service = new RequestParameterConverterService();
  }

  @Override
  protected void tearDown() throws Exception {
    LifecycleHelper.stop(service);
    LifecycleHelper.close(service);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.setParameterForPayload(SAVE_PARAM);
    return service;
  }

  public void testServiceWithMissingContentTypeKey() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    start(service);

    service.doService(msg);
    assertEquals("Payload should be ignored", payload, msg.getStringPayload());
    assertTrue("Metadata size = 0", msg.getMetadata().size() == 0);
  }

  public void testServiceWithContentTypeValueMismatch() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/xml");
    start(service);
    service.doService(msg);
    assertEquals("Payload should be ignored", payload, msg.getStringPayload());
    assertEquals("Metadata Count", 1, msg.getMetadata().size());
  }

  public void testServiceWithSaveParam() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    service.setParameterForPayload(SAVE_PARAM);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");

    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", TEST_VALUE, msg.getStringPayload());
    assertEquals("Metadata Count", 11, msg.getMetadata().size());
  }

  public void testXmlRequestParmWithSaveParam() throws Exception {
    String payload = formatAsFormData(createProperties(true), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    service.setParameterForPayload(SAVE_PARAM);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");

    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", XML_VALUE, msg.getStringPayload());
    assertEquals("Metadata Count", 11, msg.getMetadata().size());
  }

  public void testServiceWithoutSaveParam() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");
    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", payload, msg.getStringPayload());
    assertEquals("Metadata Count", 12, msg.getMetadata().size());
    assertEquals("Metadata value", TEST_VALUE, msg.getMetadataValue(SAVE_PARAM));
  }

  public void testServiceWithInferredCharset() throws Exception {
    String payload = formatAsFormData(createProperties(), "ISO-8859-1");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE,
        "application/x-www-form-urlencoded; charset=ISO-8859-1");
    service.setParameterForPayload(SAVE_PARAM);
    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", TEST_VALUE, msg.getStringPayload());
    assertEquals("Metadata Count", 11, msg.getMetadata().size());
    assertEquals("Message Encoding", "ISO-8859-1", msg.getCharEncoding());
  }

  private Properties createProperties() {
    return createProperties(false);
  }

  private Properties createProperties(boolean useXml) {
    Properties p = new Properties();
    for (int i = 0; i < 10; i++) {
      p.setProperty("Key" + i, "Value" + i);
    }
    if (useXml) {
      p.setProperty(SAVE_PARAM, XML_VALUE);
    }
    else {
      p.setProperty(SAVE_PARAM, TEST_VALUE);
    }
    return p;
  }

  private static String formatAsFormData(Properties p, String encoding)
      throws UnsupportedEncodingException {
    StringBuffer sb = new StringBuffer();
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = i.next().toString();
      String value = p.getProperty(key);
      sb.append(key).append("=").append(URLEncoder.encode(value, encoding));
      sb.append("&");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
