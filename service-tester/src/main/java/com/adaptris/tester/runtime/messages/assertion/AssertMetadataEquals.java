package com.adaptris.tester.runtime.messages.assertion;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

@XStreamAlias("assert-metadata-equals")
public class AssertMetadataEquals extends MetadataAssertion {

  @Override
  public AssertionResult execute(Map<String, String> actual) {
    return new AssertionResult(getUniqueId(), "assert-metadata-equals", getMessageHeaders().equals(actual));
  }
}
