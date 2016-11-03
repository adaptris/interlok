package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

@XStreamAlias("assert-metadata-contains")
public class AssertMetadataContains extends MetadataAssertion {

  public AssertMetadataContains(){
    super();
  }

  public AssertMetadataContains(KeyValuePairSet metadata){
    super(metadata);
  }

  public AssertMetadataContains(Map<String, String> metadata){
    super(new KeyValuePairSet(metadata));
  }

  @Override
  public AssertionResult execute(Map<String, String> actual) {
    String testType = "assert-metadata-contains";
    for(Map.Entry<String, String> entry :  getMessageHeaders().entrySet()){
      if(!(actual.containsKey(entry.getKey()) && actual.get(entry.getKey()).equals(entry.getValue()))){
        String message = String.format("Assertion Failure: [%s] metadata does not contain kvp: {%s=%s}", testType, entry.getKey(), entry.getValue());
        return new AssertionResult(getUniqueId(), testType, false, message);
      }
    }
    return new AssertionResult(getUniqueId(), testType, true);
  }
}
