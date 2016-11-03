package com.adaptris.tester.runtime.messages;

import com.adaptris.tester.runtime.messages.metadata.InlineMetadataProvider;
import com.adaptris.tester.runtime.messages.metadata.MetadataProvider;
import com.adaptris.tester.runtime.messages.payload.InlinePayloadProvider;
import com.adaptris.tester.runtime.messages.payload.PayloadProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

@XStreamAlias("test-message-provider")
public class TestMessageProvider extends TestMessage {

  private MetadataProvider metadataProvider;

  private PayloadProvider payloadProvider;

  public TestMessageProvider(){
    setPayloadProvider(new InlinePayloadProvider());
    setMetadataProvider(new InlineMetadataProvider());
  }

  public TestMessageProvider(MetadataProvider metadataProvider, PayloadProvider payloadProvider){
    setMetadataProvider(metadataProvider);
    setPayloadProvider(payloadProvider);
  }

  @Override
  public Map<String, String> getMessageHeaders()  {
    return getMetadataProvider().getMessageHeaders();
  }

  public void setMetadataProvider(MetadataProvider metadataProvider) {
    this.metadataProvider = metadataProvider;
  }

  public MetadataProvider getMetadataProvider() {
    return metadataProvider;
  }

  @Override
  public String getPayload() {
    return getPayloadProvider().getPayload();
  }

  public void setPayloadProvider(PayloadProvider payloadProvider) {
    this.payloadProvider = payloadProvider;
  }

  public PayloadProvider getPayloadProvider() {
    return payloadProvider;
  }
}
