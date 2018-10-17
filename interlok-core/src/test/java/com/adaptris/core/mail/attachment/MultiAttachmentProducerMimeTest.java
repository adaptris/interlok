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

import static com.adaptris.core.mail.attachment.MimeMailCreatorTest.create;
import static com.adaptris.core.services.mime.MimeJunitHelper.PAYLOAD_2;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.mail.MailProducerExample;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.util.text.mime.SelectByPosition;

public class MultiAttachmentProducerMimeTest extends MailProducerExample {

  private MultiAttachmentSmtpProducer producer;

  public MultiAttachmentProducerMimeTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    producer = new MultiAttachmentSmtpProducer();
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

    MimeMailCreator mmc = new MimeMailCreator();
    mmc.setBodySelector(new SelectByPosition(1));
    producer.setMailCreator(mmc);

    StandaloneProducer result = new StandaloneProducer();
    result.setProducer(producer);

    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + "-MimeMailCreator";
  }

  @Override
  protected String getExampleCommentHeader(Object obj) {
    try {
      return super.getExampleCommentHeader(obj) + "<!-- The example document for this would be \n" + create().getContent()
          + "\nwhich will create 2 attachments to the email \n" + "The email itself has the body '" + PAYLOAD_2 + "'\n-->\n";
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
