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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.CoreException;
import com.adaptris.core.FixedIntervalPoller;
import com.adaptris.core.Poller;
import com.adaptris.core.QuartzCronPoller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.util.TimeInterval;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class FtpConsumerCase extends FtpConsumerExample {


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
    FtpConsumer consumer = new FtpConsumer();
    FileTransferConnection con = createConnectionForExamples();
    consumer.setProcDirectory("/proc");
    consumer.setFtpEndpoint(getScheme() + "://overrideuser:overridepassword@hostname:port/path/to/directory");
    consumer.setFileFilterImp("*.xml");
    consumer.setPoller(pollingImp);
    StandaloneConsumer result = new StandaloneConsumer();
    result.setConnection(con);
    result.setConsumer(consumer);
    return result;
  }

  @Test
  public void testSetQuietPeriod() throws Exception {
    FtpConsumer consumer = new FtpConsumer();
    TimeInterval defaultInterval = new TimeInterval(0L, TimeUnit.SECONDS);
    assertNull(consumer.getQuietInterval());
    assertEquals(defaultInterval.toMilliseconds(), consumer.olderThanMs());

    TimeInterval interval = new TimeInterval(20L, TimeUnit.SECONDS);

    consumer.setQuietInterval(interval);
    assertEquals(interval, consumer.getQuietInterval());
    assertEquals(interval.toMilliseconds(), consumer.olderThanMs());


    consumer.setQuietInterval(null);
    assertNull(consumer.getQuietInterval());
    assertEquals(defaultInterval.toMilliseconds(), consumer.olderThanMs());
  }

  @Test
  public void testInit_UnknownFileFilter() throws Exception {
    FtpConsumer ftpConsumer = new FtpConsumer();
    ftpConsumer.setFtpEndpoint(getDestinationString());
    ftpConsumer.setFileFilterImp(".*");
    ftpConsumer.setFileFilterImp("BlahDeBlahDeBlah");
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    try {
      ftpConsumer.init();
      ftpConsumer.close();
      fail();
    }
    catch (CoreException expected) {

    }
  }

  @Test
  public void testInit_WorkDir() throws Exception {
    FtpConsumer ftpConsumer = new FtpConsumer();
    ftpConsumer.setFtpEndpoint(getDestinationString());
    ftpConsumer.setFileFilterImp(null);
    ftpConsumer.setWorkDirectory(null);
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    try {
      ftpConsumer.init();
      ftpConsumer.close();
      fail();
    }
    catch (CoreException expected) {

    }
    ftpConsumer.setWorkDirectory("work");
    ftpConsumer.init();
    ftpConsumer.close();
    ftpConsumer.setWorkDirectory("/work");
    ftpConsumer.init();
    ftpConsumer.close();
  }

  @Test
  public void testInit_ProcDir() throws Exception {
    FtpConsumer ftpConsumer = new FtpConsumer();
    ftpConsumer.setFtpEndpoint(getDestinationString());
    ftpConsumer.setWorkDirectory("/work");
    ftpConsumer.setPoller(new QuartzCronPoller("*/1 * * * * ?"));
    ftpConsumer.init();
    ftpConsumer.close();
    ftpConsumer.setProcDirectory("/proc");
    ftpConsumer.init();
    ftpConsumer.close();
    ftpConsumer.setProcDirectory("proc");
    ftpConsumer.init();
    ftpConsumer.close();
  }

  protected abstract FileTransferConnection createConnectionForExamples();

  protected abstract String getScheme();

  protected String getDestinationString() {
    return getScheme() + "://localhost" + EmbeddedFtpServer.DEFAULT_HOME_DIR;
  }

}
