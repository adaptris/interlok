/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.adaptris.core.services.metadata;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Properties;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.http.Http;

public class FormDataToMetadataTest extends MetadataServiceExample {

  private static final String TEST_VALUE = "the quick brown fox jumps over the lazy dog";
  private static final String XML_VALUE =
      "<?xml version=\"1.0\" " + "encoding=\"UTF-8\"?>" + System.lineSeparator() + "<root>"
          + "<unencrypted>Unencrypted xpath</unencrypted>" + "</root>";

  private static final String COMPLEX_PARAM = "complexParam";

  @Override
  protected FormDataToMetadata retrieveObjectForSampleConfig() {
    return new FormDataToMetadata().withContentTypeKey("Content-Type");
  }

  public void testService_NoContentType() throws Exception {
    String payload = formatAsFormData(createProperties());
    FormDataToMetadata service = new FormDataToMetadata().withMetadataPrefix(null);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    execute(service, msg);
    assertEquals(payload, msg.getContent());
    assertEquals(0, msg.getMetadata().size());
  }

  public void testService_ContentTypeNotFormData() throws Exception {
    String payload = formatAsFormData(createProperties());
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/xml");
    FormDataToMetadata service = new FormDataToMetadata();
    execute(service, msg);
    assertEquals(payload, msg.getContent());
    // mleMarker
    assertEquals(1, msg.getMetadata().size());
  }

  public void testService() throws Exception {
    String payload = formatAsFormData(createProperties(true));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");
    FormDataToMetadata service = new FormDataToMetadata();
    execute(service, msg);
    // 11 items added; + mlemarker
    assertEquals(12, msg.getMetadata().size());
    assertEquals(XML_VALUE, msg.getMetadataValue(COMPLEX_PARAM));
  }

  public void testService_Failure() throws Exception {
    String payload = formatAsFormData(createProperties());
    AdaptrisMessage msg = new DefectiveMessageFactory(WhenToBreak.METADATA_GET).newMessage(payload);
    msg.addMetadata(Http.CONTENT_TYPE, "application/x-www-form-urlencoded");
    FormDataToMetadata service = new FormDataToMetadata();
    try {
      execute(service, msg);
      fail();
    } catch (ServiceException expected) {

    }
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
      p.setProperty(COMPLEX_PARAM, XML_VALUE);
    } else {
      p.setProperty(COMPLEX_PARAM, TEST_VALUE);
    }
    return p;
  }

  private static String formatAsFormData(Properties p) throws UnsupportedEncodingException {
    StringBuffer sb = new StringBuffer();
    for (Iterator i = p.keySet().iterator(); i.hasNext();) {
      String key = i.next().toString();
      String value = p.getProperty(key);
      sb.append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
      sb.append("&");
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }
}
