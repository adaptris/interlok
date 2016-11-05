package com.adaptris.tester.runtime.messages;

import com.adaptris.tester.runtime.messages.metadata.InlineMetadataProvider;
import com.adaptris.tester.runtime.messages.metadata.MetadataProvider;
import com.adaptris.tester.runtime.messages.payload.InlinePayloadProvider;
import com.adaptris.tester.runtime.messages.payload.PayloadProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("test-message-provider")
public class TestMessageProvider {

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


  public void setMetadataProvider(MetadataProvider metadataProvider) {
    this.metadataProvider = metadataProvider;
  }

  public MetadataProvider getMetadataProvider() {
    return metadataProvider;
  }

  public void setPayloadProvider(PayloadProvider payloadProvider) {
    this.payloadProvider = payloadProvider;
  }

  public PayloadProvider getPayloadProvider() {
    return payloadProvider;
  }

  public TestMessage createTestMessage() throws MessageException{
    getMetadataProvider().init();
    getPayloadProvider().init();
    return new TestMessage(getMetadataProvider().getMessageHeaders(), getPayloadProvider().getPayload());
  }
}
