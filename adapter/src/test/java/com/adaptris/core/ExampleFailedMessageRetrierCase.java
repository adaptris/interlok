/*
 * $RCSfile: ExampleFailedMessageRetrierCase.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/05/01 15:44:16 $
 * $Author: lchan $
 */
package com.adaptris.core;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which
 * provides a method for marshaling sample XML config.
 * </p>
 */
public abstract class ExampleFailedMessageRetrierCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "FailedMessageRetrierCase.baseDir";

  public ExampleFailedMessageRetrierCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);

    Adapter w = (Adapter) object;

    result = result + configMarshaller.marshal(w);
    return result;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return this.getClass().getName();
  }
}
