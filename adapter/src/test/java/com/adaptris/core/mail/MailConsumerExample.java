package com.adaptris.core.mail;

import com.adaptris.core.AdaptrisPollingConsumer;
import com.adaptris.core.ConsumerCase;
import com.adaptris.core.StandaloneConsumer;

public abstract class MailConsumerExample extends ConsumerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "MailConsumerExamples.baseDir";

  public MailConsumerExample(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createBaseFileName(Object object) {
    AdaptrisPollingConsumer c = (AdaptrisPollingConsumer) ((StandaloneConsumer) object).getConsumer();
    return super.createBaseFileName(object) + "-" + c.getPoller().getClass().getSimpleName();
  }
}
