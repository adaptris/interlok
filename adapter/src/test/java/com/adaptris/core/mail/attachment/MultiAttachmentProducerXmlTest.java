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

package com.adaptris.core.mail.attachment;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.mail.MailProducerExample;
import com.adaptris.core.metadata.RegexMetadataFilter;

@SuppressWarnings("deprecation")
public class MultiAttachmentProducerXmlTest extends MailProducerExample {

  private static final String EXAMPLE_XML_FOR_CFG = "<document>"
      + System.lineSeparator()
      + "  <content>The text body of the email</content>"
      + System.lineSeparator()
      + "  <attachment encoding=\"base64\" filename=\"attachment1.txt\">UXVpY2sgemVwaHlycyBibG93LCB2ZXhpbmcgZGFmdCBKaW0=</attachment>"
      + System.lineSeparator()
      + "  <attachment encoding=\"base64\" filename=\"attachment2.txt\">UGFjayBteSBib3ggd2l0aCBmaXZlIGRvemVuIGxpcXVvciBqdWdz</attachment>"
      + System.lineSeparator() + "</document>";

  private MultiAttachmentSmtpProducer producer;

  public MultiAttachmentProducerXmlTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    producer = new MultiAttachmentSmtpProducer();
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj)
        + "<!-- The example document for this would be \n"
        + EXAMPLE_XML_FOR_CFG
        + "\nwhich will create 2 attachments to the email \n"
 + "The email itself has the body 'The text body of the email'\n-->\n";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ConfiguredProduceDestination dest = new ConfiguredProduceDestination();
    dest.setDestination("user@domain");

    producer.setDestination(dest);
    producer.setSubject("Configured subject");
    producer.setSmtpUrl("smtp://localhost:25");
    producer.setCcList("user@domain, user@domain");
    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern("X-Email.*");
    producer.setMetadataFilter(filter);

    XmlMailCreator mmc = new XmlMailCreator();
    mmc.setAttachmentHandler(new XmlAttachmentHandler("/document/attachment",
        "@filename", "@encoding"));
    mmc.setBodyHandler(new XmlBodyHandler("/document/content", "text/plain"));
    producer.setMailCreator(mmc);
    StandaloneProducer result = new StandaloneProducer();
    result.setProducer(producer);

    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-XmlMailCreator";
  }
}
