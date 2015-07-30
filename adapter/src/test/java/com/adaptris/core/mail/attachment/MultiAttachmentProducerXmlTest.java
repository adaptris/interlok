/*
 * $RCSfile: MultiAttachmentProducerXmlTest.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/05/01 15:43:36 $
 * $Author: lchan $
 */
package com.adaptris.core.mail.attachment;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.mail.MailProducerExample;

@SuppressWarnings("deprecation")
public class MultiAttachmentProducerXmlTest extends MailProducerExample {

  private static final String EXAMPLE_XML_FOR_CFG = "<document>"
      + System.getProperty("line.separator")
      + "  <content>The text body of the email</content>"
      + System.getProperty("line.separator")
      + "  <attachment encoding=\"base64\" filename=\"attachment1.txt\">UXVpY2sgemVwaHlycyBibG93LCB2ZXhpbmcgZGFmdCBKaW0=</attachment>"
      + System.getProperty("line.separator")
      + "  <attachment encoding=\"base64\" filename=\"attachment2.txt\">UGFjayBteSBib3ggd2l0aCBmaXZlIGRvemVuIGxpcXVvciBqdWdz</attachment>"
      + System.getProperty("line.separator") + "</document>";

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
    producer.setSendMetadataAsHeaders(true);
    producer.setSendMetadataRegexp("X-MyHeaders.*");

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
