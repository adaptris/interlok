/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.http.Http;

@SuppressWarnings("deprecation")
public class RequestParameterConverterServiceTest extends HttpServiceExample {

  private static final String TEST_VALUE = "the quick brown fox jumps over the lazy dog";
  private static final String XML_VALUE = "<?xml version=\"1.0\" "
      + "encoding=\"UTF-8\"?>" + System.lineSeparator()
      + "<root>" + "<unencrypted>Unencrypted xpath</unencrypted>" + "</root>";

  private static final String SAVE_PARAM = "PostParameterToSaveAsPayload";
  private RequestParameterConverterService service;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Before
  public void setUp() throws Exception {
    service = new RequestParameterConverterService();
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stop(service);
    LifecycleHelper.close(service);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    service.setParameterForPayload(SAVE_PARAM);
    return service;
  }

  @Test
  public void testServiceWithMissingContentTypeKey() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    start(service);

    service.doService(msg);
    assertEquals("Payload should be ignored", payload, msg.getContent());
    assertTrue("Metadata size = 0", msg.getMetadata().size() == 0);
  }

  @Test
  public void testServiceWithContentTypeValueMismatch() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/xml");
    start(service);
    service.doService(msg);
    assertEquals("Payload should be ignored", payload, msg.getContent());
    assertEquals("Metadata Count", 1, msg.getMetadata().size());
  }

  @Test
  public void testServiceWithSaveParam() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    service.setParameterForPayload(SAVE_PARAM);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");

    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", TEST_VALUE, msg.getContent());
    assertEquals("Metadata Count", 11, msg.getMetadata().size());
  }

  @Test
  public void testXmlRequestParmWithSaveParam() throws Exception {
    String payload = formatAsFormData(createProperties(true), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    service.setParameterForPayload(SAVE_PARAM);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");

    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", XML_VALUE, msg.getContent());
    assertEquals("Metadata Count", 11, msg.getMetadata().size());
  }

  @Test
  public void testServiceWithoutSaveParam() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");
    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", payload, msg.getContent());
    assertEquals("Metadata Count", 12, msg.getMetadata().size());
    assertEquals("Metadata value", TEST_VALUE, msg.getMetadataValue(SAVE_PARAM));
  }

  @Test
  public void testService_Failure() throws Exception {
    String payload = formatAsFormData(createProperties(), "UTF-8");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    // Use an invalid MIME type which should cause the ContentType constructor to fail.
    msg.addMetadata(Http.CONTENT_TYPE, "/x-www-form-urlencoded");
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException e) {

    }
  }

  @Test
  public void testServiceWithInferredCharset() throws Exception {
    String payload = formatAsFormData(createProperties(), "ISO-8859-1");
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance()
        .newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE,
        "application/x-www-form-urlencoded; charset=ISO-8859-1");
    service.setParameterForPayload(SAVE_PARAM);
    start(service);

    service.doService(msg);
    assertEquals("Payload Equality", TEST_VALUE, msg.getContent());
    assertEquals("Metadata Count", 11, msg.getMetadata().size());
    assertEquals("Message Encoding", "ISO-8859-1", msg.getContentEncoding());
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
