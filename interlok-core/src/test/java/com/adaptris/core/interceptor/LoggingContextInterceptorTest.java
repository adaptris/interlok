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

package com.adaptris.core.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.MDC;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.stubs.MockChannel;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.interlok.junit.scaffolding.BaseCase;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;

public class LoggingContextInterceptorTest {

  @Rule
  public TestName testName = new TestName();
  @Before
  public void setUp() {

  }

  @After
  public void tearDown() {

  }

  @Test
  public void testInterceptor() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor();
    interceptor.setKey(testName.getMethodName());
    interceptor.setValue(testName.getMethodName());
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflow", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      assertTrue(metadata.containsKey(testName.getMethodName()));
      assertEquals(testName.getMethodName(), metadata.get(testName.getMethodName()));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_UniqueId() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor("MyInterceptor");
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflow", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      assertTrue(metadata.containsKey("MyInterceptor"));
      assertEquals("MyInterceptor", metadata.get("MyInterceptor"));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_KVP() throws Exception {
    KeyValuePairList kvp = new KeyValuePairList();
    kvp.add(new KeyValuePair("key1", "value1"));
    kvp.add(new KeyValuePair("key2", "value2"));
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    interceptor.setValuesToSet(kvp);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflowName", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.setUniqueId("channelName");
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      assertTrue(metadata.containsKey("key1"));
      assertEquals("value1", metadata.get("key1"));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_KVP_Expression() throws Exception {
    KeyValuePairList kvp = new KeyValuePairList();
    kvp.add(new KeyValuePair("key1", "%message{mk1}"));
    kvp.add(new KeyValuePair("key2", "%message{mk2}"));
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    interceptor.setValuesToSet(kvp);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflowName", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.setUniqueId("channelName");
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata("mk1", "mv1");
      msg.addMetadata("mk2", "mv2");
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      assertTrue(metadata.containsKey("key1"));
      assertEquals("mv1", metadata.get("key1"));
      assertTrue(metadata.containsKey("key2"));
      assertEquals("mv2", metadata.get("key2"));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_Defaults() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    interceptor.setAddDefaultKeysAsObjectMetadata(true);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflowName", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.setUniqueId("channelName");
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      Map<Object, Object> object = prod.getMessages().get(0).getObjectHeaders();
      assertTrue(metadata.containsKey(CoreConstants.CHANNEL_ID_KEY));
      assertEquals("channelName", metadata.get(CoreConstants.CHANNEL_ID_KEY));
      assertTrue(metadata.containsKey(CoreConstants.WORKFLOW_ID_KEY));
      assertEquals("workflowName", metadata.get(CoreConstants.WORKFLOW_ID_KEY));
      assertTrue(metadata.containsKey(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
      assertEquals(msg.getUniqueId(), metadata.get(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
      assertTrue(object.containsKey(CoreConstants.CHANNEL_ID_KEY));
      assertEquals("channelName", object.get(CoreConstants.CHANNEL_ID_KEY));
      assertTrue(object.containsKey(CoreConstants.WORKFLOW_ID_KEY));
      assertEquals("workflowName", object.get(CoreConstants.WORKFLOW_ID_KEY));
      assertTrue(metadata.containsKey(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
      assertEquals(msg.getUniqueId(), object.get(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_Defaults_NotAsObjectMetadata() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflowName", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.setUniqueId("channelName");
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      Map<Object, Object> object = prod.getMessages().get(0).getObjectHeaders();
      assertTrue(metadata.containsKey(CoreConstants.CHANNEL_ID_KEY));
      assertEquals("channelName", metadata.get(CoreConstants.CHANNEL_ID_KEY));
      assertTrue(metadata.containsKey(CoreConstants.WORKFLOW_ID_KEY));
      assertEquals("workflowName", metadata.get(CoreConstants.WORKFLOW_ID_KEY));
      assertTrue(metadata.containsKey(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
      assertEquals(msg.getUniqueId(), metadata.get(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
      assertFalse(object.containsKey(CoreConstants.CHANNEL_ID_KEY));
      assertFalse(object.containsKey(CoreConstants.WORKFLOW_ID_KEY));
      assertFalse(object.containsKey(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_NoDefaults() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    interceptor.setUseDefaultKeys(false);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflowName", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.setUniqueId("channelName");
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      Map<Object, Object> object = prod.getMessages().get(0).getObjectHeaders();
      assertFalse(metadata.containsKey(CoreConstants.CHANNEL_ID_KEY));
      assertFalse(metadata.containsKey(CoreConstants.WORKFLOW_ID_KEY));
      assertFalse(metadata.containsKey(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
      assertFalse(object.containsKey(CoreConstants.CHANNEL_ID_KEY));
      assertFalse(object.containsKey(CoreConstants.WORKFLOW_ID_KEY));
      assertFalse(object.containsKey(CoreConstants.MESSAGE_UNIQUE_ID_KEY));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_WorkflowName() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow("workflow", interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      assertTrue(metadata.containsKey("workflow"));
      assertEquals("workflow", metadata.get("workflow"));
    } finally {
      BaseCase.stop(c);
    }
  }


  @Test
  public void testInterceptor_ChannelName() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor(null);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow(null, interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      assertTrue(metadata.containsKey(c.getUniqueId()));
      assertEquals(c.getUniqueId(), metadata.get(c.getUniqueId()));
    } finally {
      BaseCase.stop(c);
    }
  }

  @Test
  public void testInterceptor_GuidOnly() throws Exception {
    LoggingContextWorkflowInterceptor interceptor = new LoggingContextWorkflowInterceptor();
    interceptor.setUniqueId(null);
    StandardWorkflow wf = StatisticsMBeanCase.createWorkflow(null, interceptor);
    MockMessageProducer prod = new MockMessageProducer();
    wf.setProducer(prod);
    wf.getServiceCollection().add(new LoggingContextToMetadata());
    MockChannel c = new MockChannel();
    c.setUniqueId(null);
    c.getWorkflowList().add(wf);
    try {
      BaseCase.start(c);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      wf.onAdaptrisMessage(msg);
      Map<String, String> metadata = prod.getMessages().get(0).getMessageHeaders();
      metadata.remove(CoreConstants.MLE_SEQUENCE_KEY);
      // At this point we have no idea what the metadata key will be so just check the size.
      assertTrue(metadata.size() >= 1);
    } finally {
      BaseCase.stop(c);
    }
  }


  private class LoggingContextToMetadata extends ServiceImp {

    private LoggingContextToMetadata() {

    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      Map<String, String> map = MDC.getCopyOfContextMap();
      for (Map.Entry<String, String> entry : map.entrySet()) {
        msg.addMetadata(entry.getKey(), entry.getValue());
      }
    }

    @Override
    public void prepare() throws CoreException {
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
    }

  }
}
