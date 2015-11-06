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

package com.adaptris.core.services;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.GeneralServiceExample;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.ServiceList;
import com.adaptris.core.util.LifecycleHelper;

public class Perf4jTimingServiceTest extends GeneralServiceExample {

  public Perf4jTimingServiceTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }


  public void testSetService() {
    NullService n = new NullService();
    Perf4jTimingService s = new Perf4jTimingService();
    s.setService(n);
    assertEquals(n, s.getService());
    try {
      s.setService(null);
      fail("null setService");
    }
    catch (IllegalArgumentException expected) {
      ;
    }
  }

  public void testSetIncludeLifecycleStats() throws Exception {
    Perf4jTimingService s = new Perf4jTimingService();
    s.setIncludeLifecycleStats(Boolean.TRUE);
    assertTrue(s.getIncludeLifecycleStats());
  }

  public void testSetLogCategory() throws Exception {
    Perf4jTimingService s = new Perf4jTimingService();
    s.setLogCategory("my.logger");
    assertEquals("my.logger", s.getLogCategory());
    s.setLogCategory(null);
    assertNull(s.getLogCategory());
  }

  public void testSetTag() throws Exception {
    Perf4jTimingService s = new Perf4jTimingService();
    s.setTag("my.tag");
    assertEquals("my.tag", s.getTag());
    s.setTag(null);
    assertNull(s.getTag());
  }

  public void testServiceNoLifecycleStats() throws Exception {
    Perf4jTimingService s = createService();
    s.setIncludeLifecycleStats(false);
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  public void testServiceLifecycleStats() throws Exception {
    Perf4jTimingService s = createService();
    s.setIncludeLifecycleStats(true);
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());

  }

  public void testServiceWithCategory() throws Exception {
    Perf4jTimingService s = createService();
    s.setLogCategory(this.getClass().getCanonicalName());
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  public void testServiceWithNoCategory() throws Exception {
    Perf4jTimingService s = createService();
    s.setLogCategory("");
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    s.setLogCategory(null);
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  public void testServiceWithNoTag() throws Exception {
    Perf4jTimingService s = createService();
    s.setTag("");
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
    s.setTag(null);
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }


  public void testServiceWithTag() throws Exception {
    Perf4jTimingService s = createService();
    s.setTag("my.tag");
    execute(s, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  public void testWithFailOnInit() throws Exception {
    FailingService n = new FailingService(FailingService.WhenToFail.ON_INIT);
    Perf4jTimingService s = new Perf4jTimingService();
    s.setIncludeLifecycleStats(true);
    s.setService(n);
    try {
      LifecycleHelper.init(s);
      fail("successful init");
    }
    catch (CoreException expected) {

    }
    finally {
      stop(s);
    }
  }

  public void testWithFailOnStart() throws Exception {
    FailingService n = new FailingService(FailingService.WhenToFail.ON_START);
    Perf4jTimingService s = new Perf4jTimingService();
    s.setIncludeLifecycleStats(true);
    s.setService(n);
    LifecycleHelper.init(s);
    try {
      LifecycleHelper.start(s);
      fail("successful start");
    }
    catch (CoreException expected) {

    }
    finally {
      stop(s);
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    Perf4jTimingService result = createService();
    result.setTag("MyTagName");
    result.setIncludeLifecycleStats(Boolean.FALSE);
    return result;
  }

  private Perf4jTimingService createService() {
    Perf4jTimingService s = new Perf4jTimingService();
    s.setService(new ServiceList(new Service[]
    {
      new NullService()
    }));
    return s;
  }

  private static class FailingService extends ServiceImp {

    public enum WhenToFail {
      NEVER,
      ON_INIT,
      ON_START,
      ON_PREPARE
    };

    private WhenToFail whenToFail = WhenToFail.NEVER;

    public FailingService() {
      super();
    }

    public FailingService(WhenToFail wtf) {
      this();
      whenToFail = wtf;
    }

    public void init() throws CoreException {
      if (whenToFail.equals(WhenToFail.ON_INIT)) {
        throw new CoreException(WhenToFail.ON_INIT + " failure specified");
      }
    }

    @Override
    public void start() throws CoreException {
      if (whenToFail.equals(WhenToFail.ON_START)) {
        throw new CoreException(WhenToFail.ON_START + " failure specified");
      }
      super.start();
    }

    public void close() {
    }

    @Override
    public void prepare() throws CoreException {
      if (whenToFail.equals(WhenToFail.ON_PREPARE)) {
        throw new CoreException(WhenToFail.ON_PREPARE.name());
      }
    }

    public void doService(AdaptrisMessage msg) throws ServiceException {
      throw new ServiceException("As Configured");
    }

  }
}
