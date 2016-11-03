package com.adaptris.tester.runtime.messages;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@XStreamAlias("inline-test-message")
@Deprecated
public class InlineTestMessage extends TestMessage {

  private String payload;
  private KeyValuePairSet metadata;

  public InlineTestMessage(){
    metadata = new KeyValuePairSet();
  }

  public void setMetadata(KeyValuePairSet metadata) {
    this.metadata = metadata;
  }

  public KeyValuePairSet getMetadata() {
    return metadata;
  }

  @Override
  public Map<String, String> getMessageHeaders() {
    return Collections.unmodifiableMap(toMap(metadata));
  }

  @Override
  public String getPayload() {
    return this.payload;
  }


  public void setPayload(String payload) {
    this.payload = payload;
  }

  private Map<String, String> toMap(KeyValuePairBag bag) {
    Map<String, String> result = new HashMap<>(bag.size());
    for (KeyValuePair kvp : bag) {
      result.put(kvp.getKey(), kvp.getValue());
    }
    return result;
  }
}
