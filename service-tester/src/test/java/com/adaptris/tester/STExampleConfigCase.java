package com.adaptris.tester;

import com.adaptris.core.ExampleConfigCase;

public abstract class STExampleConfigCase extends ExampleConfigCase {


  public STExampleConfigCase(String name) {
    super(name);
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);
    result = result + configMarshaller.marshal(object);
    return result;
  }

}
