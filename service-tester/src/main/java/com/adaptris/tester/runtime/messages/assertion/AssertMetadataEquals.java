package com.adaptris.tester.runtime.messages.assertion;

import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

@XStreamAlias("assert-metadata-equals")
public class AssertMetadataEquals extends MetadataAssertion {

  public AssertMetadataEquals(){
    super();
  }

  public AssertMetadataEquals(KeyValuePairSet metadata){
    super(metadata);
  }

  public AssertMetadataEquals(Map<String, String> metadata){
    super(new KeyValuePairSet(metadata));
  }

  @Override
  public AssertionResult execute(Map<String, String> actual) {
    return new AssertionResult(getUniqueId(), "assert-metadata-equals", getMessageHeaders().equals(actual));
  }
}
