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

package com.adaptris.core.mail;

import javax.mail.internet.ContentType;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.metadata.NoOpMetadataFilter;
import com.adaptris.core.services.metadata.MetadataServiceExample;
import com.adaptris.core.stubs.DefectiveMessageFactory;

public class MimeMessageHeadersTest extends MetadataServiceExample {

  private static final String EXAMPLE_MSG="Received: from 152620-EDGE01.mex07a.mlsrvr.com (192.168.1.193) by\r\n" + 
      " 152333-HUB01.mex07a.mlsrvr.com (192.168.1.195) with Microsoft SMTP Server\r\n" + 
      " (TLS) id 8.1.311.2; Thu, 6 Nov 2008 13:03:44 -0600\r\n" + 
      "Received: from gate23.gate.sat.mlsrvr.com (64.49.219.7) by\r\n" + 
      " 152620-EDGE01.mex07a.mlsrvr.com (192.168.1.193) with Microsoft SMTP Server\r\n" + 
      " (TLS) id 8.1.311.2; Thu, 6 Nov 2008 13:03:41 -0600\r\n" + 
      "From: \"hello@example.com\" <hello@example.com>\r\n" + 
      "To: email integrations <email@example.com.com>\r\n" + 
      "Date: Thu, 6 Nov 2008 13:02:53 -0600\r\n" + 
      "Subject: Hello World\r\n" + 
      "Message-ID: <110141896.1225998173422.JavaMail.hello@world>\r\n" + 
      "Reply-To: \"hello@example.com\" <hello@example.com>\r\n" + 
      "Accept-Language: en-GB, en-US\r\n" + 
      "Content-Type: multipart/alternative;\r\n" + 
      "  boundary=\"_000_1101418961225998173422JavaMailob10userob10aa3prd_\"\r\n" + 
      "MIME-Version: 1.0\r\n" + 
      "\r\n" + 
      "--_000_1101418961225998173422JavaMailob10userob10aa3prd_\r\n" + 
      "Content-Type: text/plain; charset=\"us-ascii\"\r\n" + 
      "Content-Transfer-Encoding: quoted-printable\r\n" + 
      "\r\n" + 
      "Hello World\r\n" + 
      "\r\n" + 
      "\r\n" + 
      "--_000_1101418961225998173422JavaMailob10userob10aa3prd_\r\n" + 
      "Content-Type: text/html; charset=\"us-ascii\"\r\n" + 
      "Content-Transfer-Encoding: quoted-printable\r\n" + 
      "\r\n" + 
      "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\r\n" + 
      "<html>\r\n" + 
      "\r\n" + 
      "  <body>\r\n" + 
      "    <p>Hello World</p>\r\n" + 
      "  </body>\r\n" + 
      "</html>\r\n" + 
      "\r\n" + 
      "--_000_1101418961225998173422JavaMailob10userob10aa3prd_--\r\n" + 
      "";
  
  public MimeMessageHeadersTest(String name) {
    super(name);
  }

  public void testService() throws Exception {
    MimeHeadersAsMetadataService service = retrieveObjectForSampleConfig();
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(EXAMPLE_MSG);
    execute(service, msg);
    ContentType ct = new ContentType(msg.getMetadataValue("Content-Type"));
    assertEquals("multipart/alternative", ct.getBaseType());
    assertEquals("_000_1101418961225998173422JavaMailob10userob10aa3prd_", ct.getParameter("boundary"));
    assertEquals("Hello World", msg.getMetadataValue("Subject"));
    assertEquals("email integrations <email@example.com.com>", msg.getMetadataValue("To"));
  }

  public void testService_ThrowsException() throws Exception {
    MimeHeadersAsMetadataService service = retrieveObjectForSampleConfig();
    AdaptrisMessage msg = new DefectiveMessageFactory().newMessage("Hello World");
    try {
      execute(service, msg);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  @Override
  protected MimeHeadersAsMetadataService retrieveObjectForSampleConfig() {
    MimeHeadersAsMetadataService service = new MimeHeadersAsMetadataService();
    service.setHandler(new MetadataMailHeaders("", new NoOpMetadataFilter()));
    return service;
  }


}
