package com.adaptris.tester.runtime.messages.metadata;

import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@XStreamAlias("inline-metadata-provider")
public class InlineMetadataProvider extends MetadataProvider {

  private KeyValuePairSet metadata;

  public InlineMetadataProvider(){
    setMetadata(new KeyValuePairSet());
  }

  public InlineMetadataProvider(final KeyValuePairSet metadata){
    setMetadata(metadata);
  }

  public void setMetadata(KeyValuePairSet metadata) {
    this.metadata = metadata;
  }

  public KeyValuePairSet getMetadata() {
    return metadata;
  }

  @Override
  public Map<String, String> getMessageHeaders() {
    return Collections.unmodifiableMap(toMap(getMetadata()));
  }

  private Map<String, String> toMap(KeyValuePairBag bag) {
    Map<String, String> result = new HashMap<>(bag.size());
    for (KeyValuePair kvp : bag) {
      result.put(kvp.getKey(), kvp.getValue());
    }
    return result;
  }
}
