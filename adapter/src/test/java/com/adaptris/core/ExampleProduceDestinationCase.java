package com.adaptris.core;

import com.adaptris.core.stubs.DummyMessageProducer;

/**
 * Generating Examples that contain ProduceDestinations.
 */
public abstract class ExampleProduceDestinationCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ProduceDestinationCase.baseDir";

  public ExampleProduceDestinationCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);
    StandaloneProducer w = (StandaloneProducer) object;
    w.getProducer().setMessageFactory(null);
    result = result + configMarshaller.marshal(w);
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(new DummyMessageProducer(createDestinationForExamples()));
  }

  protected abstract ProduceDestination createDestinationForExamples();

  @Override
  protected String getExampleCommentHeader(Object object) {
    return "<!--\n\nThis example simply shows the usage for a particular ProduceDestination;"
        + "\nthe wrapping producer may not be suitable for the destination at all."
        + "\nAs always, check the javadocs for more information." + "\n\n-->\n";
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneProducer) object).getProducer().getDestination().getClass().getCanonicalName();
  }

}
