package com.adaptris.core.ftp;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;

public abstract class FtpProducerCase extends FtpProducerExample {
  public FtpProducerCase(String name) {
    super(name);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createConnectionForExamples(), createProducerExample());
  }

  protected FtpProducer createProducerExample() {
    FtpProducer producer = new FtpProducer();
    producer.setBuildDirectory("/path/to/temporary/staging/area/where/files/will/be/uploaded");
    producer.setDestDirectory("/path/to/actual/directory/where/files/will/be/renamed/as/the/last/step");
    producer.setDestination(new ConfiguredProduceDestination(getScheme()
        + "://overrideuser:overridepassword@hostname:port/path/to/directory"));
    return producer;
  }

  protected abstract FileTransferConnection createConnectionForExamples();

  protected abstract String getScheme();

}
