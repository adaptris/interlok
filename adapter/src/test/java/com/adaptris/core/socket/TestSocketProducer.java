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

package com.adaptris.core.socket;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;

/**
 * @author lchan
 * @author $Author: lchan $
 */
public class TestSocketProducer extends ProducerCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "SocketProducerExamples.baseDir";

  public TestSocketProducer(String name) {
    super(name);
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    TcpProduceConnection tcp = new TcpProduceConnection();
    SocketProducer producer = new SocketProducer();
    producer.setDestination(new ConfiguredProduceDestination("tcp://localhost:9099"));
    producer
        .setProtocolImplementation("my.implementation.of.com.adaptris.core."
            + "socket.Protocol");
    return new StandaloneProducer(tcp, producer);
  }

  @Override
  public String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "\n<!--" + "\n-->";
  }
}
