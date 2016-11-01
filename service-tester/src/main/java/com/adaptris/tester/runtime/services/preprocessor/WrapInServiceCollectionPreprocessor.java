package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.util.GuidGenerator;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("wrap-in-sc-preprocessor")
public class WrapInServiceCollectionPreprocessor implements Preprocessor{
  @Override
  public String execute(String input) throws PreprocessorException {
    GuidGenerator guidGenerator = new GuidGenerator();
    String guid = guidGenerator.getUUID();
    String start = "<service-collection class=\"service-list\"><unique-id>sc-wrap-" + guid +"</unique-id><services>";
    String finish = "</services></service-collection>";
    return start + input + finish;
  }
}
