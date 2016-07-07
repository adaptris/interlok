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

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.security.password.Password;
import com.adaptris.sftp.ConfigRepositoryBuilder;
import com.adaptris.sftp.OpenSSHConfigBuilder;

public class SftpProducerWithKeyTest extends FtpProducerExample {

  public SftpProducerWithKeyTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  private StandaloneProducer createProducerExample(ConfigRepositoryBuilder behaviour) {
    SftpKeyAuthConnection con = new SftpKeyAuthConnection();
    FtpProducer producer = new FtpProducer();
    try {
      con.setPrivateKeyFilename("/path/to/private/key");
      con.setPrivateKeyPassword(Password.encode("MyPassword", Password.PORTABLE_PASSWORD));
      con.setConfiguration(behaviour);
      con.setDefaultUserName("UserName if Not configured in destination");
      producer.setFilenameCreator(new FormattedFilenameCreator());
      producer.setDestination(new ConfiguredProduceDestination("sftp://sftpuser@hostname:port/path/to/directory"));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneProducer(con, producer);
  }

  @Override
  protected String createBaseFileName(Object object) {
    SftpKeyAuthConnection con = (SftpKeyAuthConnection) ((StandaloneProducer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getConfiguration().getClass().getSimpleName()
        + "-SFTP-KeyBasedAuth";
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneProducer[]
    {
        createProducerExample(new OpenSSHConfigBuilder("/path/openssh/config/file")),
        createProducerExample(SftpConsumerTest.createPerHostConfigRepo()),
        createProducerExample(SftpConsumerTest.createInlineConfigRepo()),
    }));
  }

}
