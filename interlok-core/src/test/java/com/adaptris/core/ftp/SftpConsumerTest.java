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

import static com.adaptris.core.ftp.SftpExampleHelper.createConnectionsForExamples;
import static com.adaptris.core.ftp.SftpExampleHelper.createPollers;
import static com.adaptris.core.ftp.SftpExampleHelper.getConfigSimpleName;
import static com.adaptris.core.ftp.SftpExampleHelper.setConfigBuilder;
import java.util.ArrayList;
import java.util.List;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.Poller;
import com.adaptris.core.StandaloneConsumer;
import com.adaptris.sftp.ConfigBuilder;

public class SftpConsumerTest extends FtpConsumerCase {

  private static final String BASE_DIR_KEY = "SftpConsumerExamples.baseDir";

  public SftpConsumerTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

  private StandaloneConsumer createConsumerExample(ConfigBuilder behavior, Poller poller) {
    FileTransferConnection con = createConnectionForExamples();
    FtpConsumer cfgConsumer = new FtpConsumer();
    try {
      setConfigBuilder(con, behavior);
      cfgConsumer.setProcDirectory("/proc");
      cfgConsumer.setDestination(new ConfiguredConsumeDestination("sftp://overrideuser@hostname:port/path/to/directory", "*.xml"));
      cfgConsumer.setPoller(poller);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneConsumer(con, cfgConsumer);
  }

  private List createConsumerExamples(FileTransferConnection conn, Poller... pollers) {
    List<StandaloneConsumer> result = new ArrayList();
    try {
      for (Poller p : pollers) {
        FtpConsumer ftp = new FtpConsumer();
        ftp.setProcDirectory("/proc");
        ftp.setDestination(new ConfiguredConsumeDestination("sftp://overrideuser@hostname:port/path/to/directory", "*.xml"));
        ftp.setPoller(p);
        result.add(new StandaloneConsumer(conn, ftp));
      }
    }
    catch (Exception e) {
        throw new RuntimeException(e);
    }
    return result;
  }

  @Override
  protected List<StandaloneConsumer> retrieveObjectsForSampleConfig() {
    List<FileTransferConnection> connections = createConnectionsForExamples();
    List<StandaloneConsumer> consumers = new ArrayList<>();
    for (FileTransferConnection c : connections) {
      consumers.addAll(createConsumerExamples(c, createPollers()));
    }
    return consumers;
  }

  @Override
  protected String createBaseFileName(Object object) {
    FileTransferConnection con = (FileTransferConnection) ((StandaloneConsumer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-" + getConfigSimpleName(con);
  }

  @Override
  protected FileTransferConnection createConnectionForExamples() {
    throw new RuntimeException("Should never be executed");
  }

}
