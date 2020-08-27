/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelList;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultEventHandler;
import com.adaptris.core.NullService;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.security.access.EmptyIdentityBuilder;

public class LifecycleHelperTest extends LifecycleHelper {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testLifecycle() throws Exception {
    StateManagedComponent stateManaged = new NullService();
    ComponentLifecycle noState = new ChannelList();
    try {
      initAndStart(stateManaged);
      initAndStart(noState);
      initAndStart(null);
      stopAndClose(null);
    } finally {
      stopAndClose(stateManaged);
      stopAndClose(noState);
    }
  }

  @Test(expected = CoreException.class)
  public void testLifecycle_Failure() throws Exception {
    StateManagedComponent failingService = new NullService() {
      @Override
      public void start() throws CoreException {
        throw new CoreException();
      }
    };
    try {
      initAndStart(failingService);
    }
    finally {
      stopAndClose(failingService);
    }
  }

  @Test
  public void testRegisterEventHandler() throws Exception {
    registerEventHandler(new NullService(), new DefaultEventHandler());
    registerEventHandler(null, new DefaultEventHandler());
    registerEventHandler(new Channel(), new DefaultEventHandler());
  }

  @Test
  public void testPrepare() throws Exception {
    prepare(new NullService());
    prepare((ComponentLifecycle) null);
    prepare(new EmptyIdentityBuilder());
  }

  @Test
  public void testSleepQuietly() throws Exception {
    waitQuietly(-1);
    waitQuietly(5);
  }

}
