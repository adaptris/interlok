package com.adaptris.tester.runtime.messages.payload;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("inline-payload-provider")
public class InlinePayloadProvider implements PayloadProvider {

  private String payload;

  public InlinePayloadProvider(){
    this.payload = "";
  }

  public InlinePayloadProvider(final String payload){
    this.payload = payload;
  }

  @Override
  public String getPayload() {
    return this.payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
