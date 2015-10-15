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

package com.adaptris.core.jms;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.jndi.SimpleFactoryConfiguration;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class JndiExtraConfigPtpProducerTest extends JmsProducerCase {

  static final String DEFAULT_FILE_SUFFIX = "-ExtraConfig-JNDI";
  static final String DEFAULT_XML_COMMENT = "<!-- " + "\nNote that using StandardJndiImplementation means that"
      + "\nthe JmsConnection fields broker-host, broker-url, port are ignored."
      + "\nCheck your JNDI provider documentation for the correct values to put into jndi-params."
      + "\n\nIn this example we are applying additional configuration to the ConnectionFactory"
      + "\nafter it has been retrieved from JNDI, by using an ExtraFactoryConfiguration"
      + "\nimplementation. Check the javadocs for more details." + "\n-->\n";

  public JndiExtraConfigPtpProducerTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  static JmsConnection createJndiVendorImpExample(JmsConnection c) {
    return createJndiVendorImpExample(new StandardJndiImplementation(), c);
  }

  static JmsConnection createJndiVendorImpExample(StandardJndiImplementation jndi, JmsConnection c) {
    JndiPtpProducerTest.createJndiVendorImpExample(jndi, c);
    SimpleFactoryConfiguration sfc = new SimpleFactoryConfiguration();
    KeyValuePairSet jndiExtras = sfc.getProperties();
    jndiExtras.add(new KeyValuePair("ConnectID", "MyConnectId"));
    jndiExtras.add(new KeyValuePair("PingInterval", "10"));
    jndi.setExtraFactoryConfiguration(sfc);
    return c;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createJndiVendorImpExample(new JmsConnection()), new PtpProducer(
        new ConfiguredProduceDestination("jndiReferenceToQueue")));
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + DEFAULT_FILE_SUFFIX;
  }


  @Override
  protected String getExampleCommentHeader(Object obj) {
    return super.getExampleCommentHeader(obj) + DEFAULT_XML_COMMENT;
  }
}
