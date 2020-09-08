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

package com.adaptris.core.services.aggregator;

import java.util.ArrayList;
import java.util.List;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.conditional.conditions.ConditionImpl;
import com.adaptris.core.services.splitter.MessageSplitter;
import com.adaptris.core.services.splitter.PoolingSplitJoinService;
import com.adaptris.core.services.splitter.SplitJoinService;

public abstract class AggregatingServiceExample
    extends com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   *
   */
  public static final String BASE_DIR_KEY = "AggregatingServiceExamples.baseDir";

  public AggregatingServiceExample() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  protected static List<Service> createExamples(MessageSplitter splitter, MessageAggregator aggregator) {
    return createExamples(splitter, aggregator, new LogMessageService(), new NullService());
  }

  protected static List<Service> createExamples(MessageSplitter splitter, MessageAggregator aggregator, Service... services) {
    List<Service> result = new ArrayList<Service>();
    result.add(configure(new SplitJoinService(), splitter, aggregator, services));
    result.add(configure(new PoolingSplitJoinService().withMaxThreads(5), splitter, aggregator, services));
    return result;
  }

  protected static SplitJoinService configure(SplitJoinService service, MessageSplitter splitter, MessageAggregator agg,
                                    Service... services) {
    service.setAggregator(agg);
    service.setSplitter(splitter);
    service.setService(asCollection(services));
    return service;
  }

  // Have a condition that every other call passes
  protected class EvenOddCondition extends ConditionImpl {
    private int numberOfCalls = 0;

    @Override
    public boolean evaluate(AdaptrisMessage message) throws CoreException {
      numberOfCalls++;
      return numberOfCalls % 2 == 0;
    }

    @Override
    public void close() {
      throw new RuntimeException();
    }
  }
}
