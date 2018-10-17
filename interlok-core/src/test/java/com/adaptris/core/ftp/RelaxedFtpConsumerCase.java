/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
