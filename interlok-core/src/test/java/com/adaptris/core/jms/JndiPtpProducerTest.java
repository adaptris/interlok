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

import javax.naming.Context;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class JndiPtpProducerTest
    extends com.adaptris.interlok.junit.scaffolding.jms.JmsProducerCase {

  static final String DEFAULT_XML_COMMENT = "<!-- " + "\nNote that using StandardJndiImplementation means that"
      + "\nthe JmsConnection fields broker-host, broker-url, port are ignored."
      + "\nCheck your JNDI provider documentation for the correct values to put into jndi-params."
      + "\n\nYou can apply further configuration to the connection factory objects "
      + "\nafter they have been retrieved from JNDI, by using an ExtraFactoryConfiguration"
      + "\nimplementation. Check the javadocs for more details." + "\n-->\n";

  static final String DEFAULT_FILE_SUFFIX = "-JNDI";


  static JmsConnection createJndiVendorImpExample(JmsConnection c) {
    return createJndiVendorImpExample(new StandardJndiImplementation(), c);
  }

  static JmsConnection createJndiVendorImpExample(StandardJndiImplementation jndi, JmsConnection c) {
    jndi.setJndiName("MyConnectionFactory");
    KeyValuePairSet kvps = jndi.getJndiParams();
    // jndi.getJndiParams().addKeyValuePair(
    // new KeyValuePair(Context.INITIAL_CONTEXT_FACTORY,
    // "com.sun.jndi.fscontext.RefFSContextFactory"));
    kvps.addKeyValuePair(new KeyValuePair(Context.SECURITY_PRINCIPAL, "Administrator"));
    kvps.addKeyValuePair(new KeyValuePair(Context.SECURITY_CREDENTIALS, "Administrator"));
    kvps.addKeyValuePair(new KeyValuePair("com.sonicsw.jndi.mfcontext.domain", "Domain1"));
    kvps.addKeyValuePair(new KeyValuePair(Context.INITIAL_CONTEXT_FACTORY, "com.sonicsw.jndi.mfcontext.MFContextFactory"));
    jndi.getJndiParams().addKeyValuePair(new KeyValuePair(Context.PROVIDER_URL, "tcp://localhost:2506"));
    c.setVendorImplementation(jndi);
    c.setClientId(null);
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return c;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return new StandaloneProducer(createJndiVendorImpExample(new JmsConnection()),
        new PtpProducer().withQueue("jndiReferenceToQueue"));
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
