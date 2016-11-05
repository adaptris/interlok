package com.adaptris.tester.runtime.messages.payload;

import com.adaptris.annotation.MarshallingCDATA;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("inline-payload-provider")
public class InlinePayloadProvider extends PayloadProvider {

  @MarshallingCDATA
  private String payload;

  public InlinePayloadProvider(){
    setPayload("");
  }

  public InlinePayloadProvider(final String payload){
    setPayload(payload);
  }

  @Override
  public String getPayload() {
    return this.payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }
}
