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

package com.adaptris.core.stubs;

import java.util.List;
import java.util.function.Consumer;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.Channel;
import com.adaptris.core.ListenerCallbackHelper;
import com.adaptris.core.ProduceException;

/**
 * Simple stub that wraps a MockMessageProducer. Probably of use when you want
 * to test using a StandaloneConsumer.
 *
 * @author lchan
 * @author $Author: $
 */
public class MockMessageListener implements AdaptrisMessageListener, MessageCounter {
  
  private MockMessageProducer producer;
  
  private Channel channel;

  private long waitTime = -1;

  public MockMessageListener() {
    producer = new MockMessageProducer();
  }

  public MockMessageListener(long waitTime) {
    this();
    this.waitTime = waitTime;
  }
  
  @Override
  public void onAdaptrisMessage(AdaptrisMessage msg, Consumer<AdaptrisMessage> success, Consumer<AdaptrisMessage> failure) {
    ListenerCallbackHelper.prepare(msg, success, failure);
    try {
      producer.produce(msg);
      ListenerCallbackHelper.handleSuccessCallback(msg);
    } catch (ProduceException e) {
      ListenerCallbackHelper.handleFailureCallback(msg);
    } finally {
      
    }
    if (waitTime != -1) {
      try {
        Thread.sleep(waitTime);
      }
      catch (InterruptedException e) {
        ;
      }
    }

  }

  @Override
  public List<AdaptrisMessage> getMessages() {
    return producer.getMessages();
  }

  @Override
  public int messageCount() {
    return producer.messageCount();
  }

  @Override
  public String friendlyName() {
    return "MockMessageListener";
  }

  public Channel obtainChannel() {
    return channel;
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
  }
}
