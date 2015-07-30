package com.adaptris.core.ftp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;

public abstract class RelaxedFtpConsumerCase extends FtpConsumerExample {

  public RelaxedFtpConsumerCase(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneConsumer[]
    {
        createConsumerExample(new FixedIntervalPoller()), createConsumerExample(new QuartzCronPoller("*/20 * * * * ?"))
    }));
  }

  private StandaloneConsumer createConsumerExample(Poller pollingImp) {
    RelaxedFtpConsumer consumer = new RelaxedFtpConsumer();
    FileTransferConnection con = createConnectionForExamples();
    consumer.setDestination(new ConfiguredConsumeDestination(getDestinationString(), "*.xml"));
    consumer.setPoller(pollingImp);
    return new StandaloneConsumer(con, consumer);
  }

  protected abstract FileTransferConnection createConnectionForExamples();

  protected abstract String getScheme();

  protected String getDestinationString() {
    return getScheme() + "://overrideuser:overridepassword@hostname:port/path/to/directory";
  }

}
