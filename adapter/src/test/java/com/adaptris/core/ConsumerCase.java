/*
 * $RCSfile: ConsumerCase.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/05/01 15:44:16 $
 * $Author: lchan $
 */
package com.adaptris.core;

import java.util.List;

public abstract class ConsumerCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ConsumerCase.baseDir";

  public ConsumerCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  public void testMessageEventGeneratorCreateName() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertCreateName((StandaloneConsumer) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertCreateName((StandaloneConsumer) o);
      }
    }
  }

  private void assertCreateName(StandaloneConsumer p) {
    assertEquals(p.getConsumer().getClass().getName(), p.createName());
    assertEquals(p.getConsumer().getClass().getName(), p.getConsumer().createName());
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);

    StandaloneConsumer consumer = (StandaloneConsumer) object;

    result = result + configMarshaller.marshal(consumer);

    result = result.replaceAll("com\\.adaptris\\.core\\.Standalone-consumer",
        "Dummy-Root-Element");
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneConsumer) object).getConsumer().getClass().getName();
  }
}
