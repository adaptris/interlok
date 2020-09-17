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

package com.adaptris.interlok.junit.scaffolding;

import java.util.Arrays;
import com.adaptris.core.Adapter;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelList;
import com.adaptris.core.ProcessingExceptionHandler;
import com.adaptris.core.Service;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.stubs.MockMessageConsumer;
import com.adaptris.core.stubs.StubEventHandler;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which
 * provides a method for marshaling sample XML config.
 * </p>
 */
public abstract class ExampleErrorHandlerCase extends ExampleConfigGenerator {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "ErrorHandlerCase.baseDir";

  public ExampleErrorHandlerCase() {
    super();
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);

    Adapter w = (Adapter) object;

    result = result + configMarshaller.marshal(w);
    return result;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Adapter result = new Adapter();
    result.setUniqueId("dummy-adapter");
    ProcessingExceptionHandler meh = createForExamples();
    result.setMessageErrorHandler(meh);
    result.setEventHandler(new StubEventHandler());
    result.setChannelList(new ChannelList());
    return result;
  }

  protected abstract ProcessingExceptionHandler createForExamples();

  protected Channel createChannel(MockMessageConsumer consumer, ProcessingExceptionHandler ceh, ProcessingExceptionHandler weh,
                                  Service... services) {
    StandardWorkflow workflow = createWorkflow(consumer, weh, services);
    Channel c = new Channel();
    c.setMessageErrorHandler(ceh);
    c.getWorkflowList().add(workflow);
    return c;
  }

  protected StandardWorkflow createWorkflow(MockMessageConsumer consumer, ProcessingExceptionHandler errorHandler,
                                            Service... services) {
    StandardWorkflow workflow = new StandardWorkflow();
    workflow.setMessageErrorHandler(errorHandler);
    workflow.setConsumer(consumer);
    workflow.getServiceCollection().addAll(Arrays.asList(services));
    return workflow;
  }
}
