package com.adaptris.core;

import java.util.List;

public abstract class ProducerCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ProducerCase.baseDir";

  public ProducerCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);


    StandaloneProducer producer = (StandaloneProducer) object;

    result = result + configMarshaller.marshal(producer);

    return result;
  }

  public void testMessageEventGeneratorCreateName() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertCreateName((StandaloneProducer) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertCreateName((StandaloneProducer) o);
      }
    }
  }

  private void assertCreateName(StandaloneProducer p) {
    assertEquals(p.getProducer().getClass().getName(), p.createName());
    assertEquals(p.getProducer().createName(), p.createName());
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneProducer) object).getProducer().getClass().getName();
  }
}
