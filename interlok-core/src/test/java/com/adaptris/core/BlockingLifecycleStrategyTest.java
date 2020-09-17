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

package com.adaptris.core;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.adaptris.core.lifecycle.BlockingChannelLifecycleStrategy;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class BlockingLifecycleStrategyTest extends DefaultLifecycleStrategyTest {

  public BlockingLifecycleStrategyTest() {
  }

  @Override
  protected ChannelLifecycleStrategy createStrategy() {
    return new BlockingChannelLifecycleStrategy();
  }

  @Test
  public void testChannelAutoStartFalse_Bug2341() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "Bug2341");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel();
      testChannel.setUniqueId("testAutoStart");
      testChannel.setAutoStart(Boolean.FALSE);
      list.setChannels(Arrays.asList(new Channel[]
      {
        testChannel
      }));
      list.prepare();
      start(list);
      waitFor(list, ClosedState.getInstance());
      assertEquals(ClosedState.getInstance(), testChannel.retrieveComponentState());
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Test
  public void testChannelInit_ThrowsException() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelInit_ThrowsException");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel() {
        @Override
        public void init() throws CoreException {
          super.init();
          throw new CoreException("testChannelInit_ThrowsException");
        }
      };
      testChannel.setUniqueId("testChannelInit_ThrowsException");
      list.setChannels(Arrays.asList(new Channel[]
      {
          new Channel(), testChannel
      }));
      list.prepare();
      try {
        LifecycleHelper.init(list);
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Test
  public void testChannelInit_ThrowsRuntimeException() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelInit_ThrowsRuntimeException");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel() {
        @Override
        public void init() throws CoreException {
          super.init();
          throw new RuntimeException("testChannelInit_ThrowsRuntimeException");
        }
      };
      testChannel.setUniqueId("testChannelInit_ThrowsRuntimeException");
      list.setChannels(Arrays.asList(new Channel[]
      {
          new Channel(), testChannel
      }));
      list.prepare();
      try {
        LifecycleHelper.init(list);
        fail();
      }
      catch (RuntimeException expected) {

      }

    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Test
  public void testChannelStart_ThrowsException() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelStart_ThrowsException");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel() {
        @Override
        public void start() throws CoreException {
          super.start();
          throw new CoreException("testChannelStart_ThrowsException");
        }
      };
      testChannel.setUniqueId("testChannelStart_ThrowsException");
      list.setChannels(Arrays.asList(new Channel[]
      {
          new Channel(), testChannel
      }));
      list.prepare();
      LifecycleHelper.init(list);
      try {
        LifecycleHelper.start(list);
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Test
  public void testChannelStart_ThrowsRuntimeException() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelStart_ThrowsRuntimeException");
    ChannelList list = create();
    try {
      Channel testChannel = new Channel() {
        @Override
        public void start() throws CoreException {
          super.start();
          throw new RuntimeException("testChannelStart_ThrowsRuntimeException");
        }
      };
      testChannel.setUniqueId("testChannelStart_ThrowsRuntimeException");
      list.setChannels(Arrays.asList(new Channel[]
      {
          new Channel(), testChannel
      }));
      list.prepare();
      LifecycleHelper.init(list);
      try {
        LifecycleHelper.start(list);
        fail();
      }
      catch (RuntimeException expected) {

      }
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Test
  public void testChannelInit_ExceedsTimeout() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelInit_ExceedsTimeout");
    ChannelList list = create();
    BlockingChannelLifecycleStrategy st = new BlockingChannelLifecycleStrategy(new TimeInterval(3L, TimeUnit.SECONDS));
    list.setLifecycleStrategy(st);
    try {
      Channel testChannel = new Channel() {
        @Override
        public void init() throws CoreException {
          super.init();
          // Sleep for 10 seconds surely long enough for timing issues!.
          try {
            Thread.sleep(10000L);
          }
          catch (InterruptedException ignoreAndContinue) {
            System.err.println("testChannelStart_ExceedsTimeout interrupted");
          }
        }
      };
      testChannel.setUniqueId("testChannelInit_ExceedsTimeout");
      list.setChannels(Arrays.asList(new Channel[]
      {
          new Channel(), testChannel
      }));
      list.prepare();
      try {
        LifecycleHelper.init(list);
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

  @Test
  public void testChannelStart_ExceedsTimeout() throws Exception {
    String name = renameThread(this.getClass().getSimpleName() + "_" + "testChannelStart_ExceedsTimeout");
    ChannelList list = create();
    BlockingChannelLifecycleStrategy st = new BlockingChannelLifecycleStrategy(new TimeInterval(3L, TimeUnit.SECONDS));
    list.setLifecycleStrategy(st);
    try {
      Channel testChannel = new Channel() {
        @Override
        public void start() throws CoreException {
          super.start();
          // Sleep for 10 seconds surely long enough for timing issues!.
          try {
            Thread.sleep(10000L);
          }
          catch (InterruptedException ignoreAndContinue) {
            System.err.println("testChannelStart_ExceedsTimeout interrupted");
          }
        }
      };
      testChannel.setUniqueId("testChannelStart_ExceedsTimeout");
      list.setChannels(Arrays.asList(new Channel[]
      {
          new Channel(), testChannel
      }));
      list.prepare();
      LifecycleHelper.init(list);
      try {
        LifecycleHelper.start(list);
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
      stop(list);
      renameThread(name);
    }
  }

}
