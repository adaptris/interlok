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

import static com.adaptris.core.ftp.SftpExampleHelper.createInlineConfigRepo;
import static com.adaptris.core.ftp.SftpExampleHelper.createOpensshRepo;
import static com.adaptris.core.ftp.SftpExampleHelper.createPerHostConfigRepo;
import static com.adaptris.core.ftp.SftpExampleHelper.getConfigSimpleName;
import static com.adaptris.core.ftp.SftpExampleHelper.setConfigBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.FormattedFilenameCreator;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.sftp.ConfigBuilder;

public abstract class RelaxedSftpProducerCase extends RelaxedFtpProducerCase {

  private static final String BASE_DIR_KEY = "SftpProducerExamples.baseDir";

  public RelaxedSftpProducerCase(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Override
  protected void setUp() throws Exception {
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  private StandaloneProducer createProducerExample(ConfigBuilder behaviour) {
    FileTransferConnection con = createConnectionForExamples();
    RelaxedFtpProducer producer = createProducerExample();
    try {
      setConfigBuilder(con, behaviour);
      producer.setFileNameCreator(new FormattedFilenameCreator());
      producer.setDestination(new ConfiguredProduceDestination("sftp://sftpuser@hostname:port/path/to/directory"));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    return new StandaloneProducer(con, producer);
  }

  @Override
  protected String createBaseFileName(Object object) {
    FileTransferConnection con = (FileTransferConnection) ((StandaloneProducer) object).getConnection();
    return super.createBaseFileName(object) + "-" + con.getClass().getSimpleName() + "-"
        + getConfigSimpleName(con);
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    return new ArrayList(Arrays.asList(new StandaloneProducer[]
    {
        createProducerExample(createOpensshRepo()), createProducerExample(createInlineConfigRepo()),
        createProducerExample(createPerHostConfigRepo()),
    }));
  }

  @Override
  protected String getScheme() {
    return "sftp";
  }

}
