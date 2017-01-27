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
import com.adaptris.sftp.ConfigBuilder;
import com.adaptris.sftp.OpenSSHConfigBuilder;


public class RelaxedSftpConsumerTest extends RelaxedFtpConsumerCase {

  private static final String BASE_DIR_KEY = "SftpConsumerExamples.baseDir";

  public RelaxedSftpConsumerTest(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected SftpConnection createConnectionForExamples() {
    return FtpExampleHelper.sftpConnection();
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

  private StandaloneConsumer createConsumerExample(ConfigBuilder behavior, Poller poller) {
    SftpConnection con = createConnectionForExamples();
    RelaxedFtpConsumer cfgConsumer = new RelaxedFtpConsumer();
    try {
      con.setConfiguration(behavior);
      con.setDefaultUserName("UserName if Not configured in destination");
      cfgConsumer.setDestination(new ConfiguredConsumeDestination("sftp://overrideuser@hostname:port/path/to/directory", "*.xml"));
      cfgConsumer.setPoller(poller);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneConsumer(con, cfgConsumer);
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneConsumer[]
    {
        createConsumerExample(new OpenSSHConfigBuilder("/path/openssh/config/file"), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(SftpConsumerTest.createInlineConfigRepo(), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(SftpConsumerTest.createPerHostConfigRepo(), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(SftpConsumerTest.createInlineConfigRepo(), new FixedIntervalPoller()),
        createConsumerExample(SftpConsumerTest.createPerHostConfigRepo(), new FixedIntervalPoller()),
        createConsumerExample(new OpenSSHConfigBuilder("/path/openssh/config/file"), new FixedIntervalPoller()),
    }));
  }

  @Override
  protected String createBaseFileName(Object object) {
    SftpConnection con = (SftpConnection) ((StandaloneConsumer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-"
        + con.getConfiguration().getClass().getSimpleName();
  }
}
