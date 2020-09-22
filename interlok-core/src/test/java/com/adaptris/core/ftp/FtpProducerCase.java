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

import com.adaptris.core.StandaloneProducer;

public abstract class FtpProducerCase extends FtpProducerExample {

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createConnectionForExamples(), createProducerExample());
  }

  protected FtpProducer createProducerExample() {
    FtpProducer producer = new FtpProducer();
    producer.setBuildDirectory("/path/to/temporary/staging/area/where/files/will/be/uploaded");
    producer.setDestDirectory("/path/to/actual/directory/where/files/will/be/renamed/as/the/last/step");
    producer.setFtpEndpoint(
        getScheme() + "://overrideuser:overridepassword@hostname:port/path/to/directory");
    return producer;
  }

  protected abstract FileTransferConnection createConnectionForExamples();

  protected abstract String getScheme();

}
