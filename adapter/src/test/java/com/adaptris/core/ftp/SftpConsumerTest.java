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
import com.adaptris.sftp.HostConfig;
import com.adaptris.sftp.OpenSSHConfigBuilder;
import com.adaptris.sftp.PerHostConfigBuilder;
import com.adaptris.sftp.SftpClient;
import com.adaptris.util.KeyValuePair;



public class SftpConsumerTest extends FtpConsumerCase {

  private static final String BASE_DIR_KEY = "SftpConsumerExamples.baseDir";

  public SftpConsumerTest(String name) {
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
    SftpConnection con = new SftpConnection();
    con.setDefaultUserName("default-username-if-not-specified");
    con.setDefaultPassword("default-password-if-not-specified");
    con.setKnownHostsFile("/optional/path/to/known/hosts/file");
    return con;
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

  private StandaloneConsumer createConsumerExample(ConfigBuilder behavior, Poller poller) {
    SftpConnection con = createConnectionForExamples();
    FtpConsumer cfgConsumer = new FtpConsumer();
    try {
      con.setConfiguration(behavior);
      cfgConsumer.setProcDirectory("/proc");
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
    return new ArrayList(Arrays.asList(new StandaloneConsumer[] {
        createConsumerExample(new OpenSSHConfigBuilder("/path/openssh/config/file"), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(createInlineConfigRepo(), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(createPerHostConfigRepo(), new QuartzCronPoller("*/20 * * * * ?")),
        createConsumerExample(createInlineConfigRepo(), new FixedIntervalPoller()),
        createConsumerExample(createPerHostConfigRepo(), new FixedIntervalPoller()),
        createConsumerExample(new OpenSSHConfigBuilder("/path/openssh/config/file"), new FixedIntervalPoller()),
    }));
  }


  public static ConfigBuilder createInlineConfigRepo() {
    return new InlineConfigRepositoryBuilder(false).build();
  }


  public static PerHostConfigBuilder createPerHostConfigRepo() {
    PerHostConfigBuilder inline = new PerHostConfigBuilder();
    HostConfig a = new HostConfig("my.host.com", null, -1, new KeyValuePair("StrictHostKeyChecking", "yes"),
        new KeyValuePair(SftpClient.SSH_PREFERRED_AUTHENTICATIONS, SftpClient.NO_KERBEROS_AUTH));
    HostConfig b = new HostConfig("another.host.com", null, -1, new KeyValuePair("StrictHostKeyChecking", "no"),
        new KeyValuePair(SftpClient.SSH_PREFERRED_AUTHENTICATIONS, SftpClient.NO_KERBEROS_AUTH));
    inline.getHosts().add(a);
    inline.getHosts().add(b);
    return inline;
  }

  @Override
  protected String createBaseFileName(Object object) {
    SftpConnection con = (SftpConnection) ((StandaloneConsumer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-"
        + con.getConfiguration().getClass().getSimpleName();
  }
}
