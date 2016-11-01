package com.adaptris.tester.runtime.services.preprocessor;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("find-and-replace-preprocessor")
public class FindAndReplacePreprocessor implements Preprocessor {
  private KeyValuePairSet replacementKeys;

  public FindAndReplacePreprocessor(){
    this.replacementKeys = new KeyValuePairSet();
  }

  public void setReplacementKeys(KeyValuePairSet replacementKeys) {
    this.replacementKeys = replacementKeys;
  }

  public KeyValuePairSet getReplacementKeys() {
    return replacementKeys;
  }

  @Override
  public String execute(String input) throws PreprocessorException {

    for(KeyValuePair kvp :  replacementKeys.getKeyValuePairs()){
      input = input.replaceAll(kvp.getKey(), kvp.getValue());
    }
    return input;
  }
}
