package com.adaptris.core.ftp;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;

public abstract class RelaxedFtpProducerCase extends FtpProducerExample {
  public RelaxedFtpProducerCase(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createConnectionForExamples(), createProducerExample());
  }

  protected RelaxedFtpProducer createProducerExample() {
    RelaxedFtpProducer producer = new RelaxedFtpProducer();
    producer.setDestination(new ConfiguredProduceDestination(getScheme()
        + "://overrideuser:overridepassword@hostname:port/path/to/directory"));
    return producer;
  }

  protected abstract FileTransferConnection createConnectionForExamples();

  protected abstract String getScheme();
}
