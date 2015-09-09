package com.adaptris.core.mail.attachment;

import static com.adaptris.core.mail.attachment.MimeMailCreatorTest.create;
import static com.adaptris.core.services.mime.MimeJunitHelper.PAYLOAD_2;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.mail.MailProducerExample;
import com.adaptris.util.text.mime.SelectByPosition;

@SuppressWarnings("deprecation")
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
    producer.setSendMetadataAsHeaders(true);
    producer.setSendMetadataRegexp("X-MyHeaders.*");

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
      return super.getExampleCommentHeader(obj) + "<!-- The example document for this would be \n" + create().getStringPayload()
          + "\nwhich will create 2 attachments to the email \n" + "The email itself has the body '" + PAYLOAD_2 + "'\n-->\n";
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
