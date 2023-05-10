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

package com.adaptris.core.runtime;

import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_FAILED_MESSAGE_RETRIER_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultFailedMessageRetrier;
import com.adaptris.core.DefaultFailedMessageRetrierJmxMBean;
import com.adaptris.core.DefaultSerializableMessageTranslator;
import com.adaptris.core.MimeEncoder;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.stubs.MockMessageProducer;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.types.SerializableMessage;
import com.adaptris.util.TimeInterval;
import com.adaptris.util.stream.StreamUtil;

public class DefaultFailedMessageRetrierJmxTest extends ComponentManagerCase {

  public DefaultFailedMessageRetrierJmxTest() {
  }


  @Test
  public void testExistsInAdapterManager() throws Exception {
    Adapter adapter = createAdapter(getName());
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName retrierObjName = createRetrierObjectName(adapterManager);
    assertTrue(adapterManager.getChildRuntimeInfoComponents().contains(retrierObjName));
  }

  @Test
  public void testMBean_GetParentId() throws Exception {
    Adapter adapter = createAdapter(getName());
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName retrierObjName = createRetrierObjectName(adapterManager);
    try {
      adapterManager.registerMBean();
      DefaultFailedMessageRetrierJmxMBean proxy = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertNotNull(proxy);
      assertEquals(getName(), proxy.getParentId());
    }
    finally {
      adapterManager.unregisterMBean();
    }

  }

  @Test
  public void testMBean_GetParentObjectName() throws Exception {

    Adapter adapter = createAdapter(getName());
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);
    AdapterManager adapterManager = new AdapterManager(adapter);
    ObjectName retrierObjName = createRetrierObjectName(adapterManager);
    try {
      adapterManager.registerMBean();
      DefaultFailedMessageRetrierJmxMBean proxy = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertNotNull(proxy);
      assertEquals(adapterManager.createObjectName(), proxy.getParentObjectName());
    }
    finally {
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_SerializableMessage() throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel channel = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    channel.getWorkflowList().add(wf);
    adapter.getChannelList().add(channel);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@" + getName());
    SerializableMessage serialized = new DefaultSerializableMessageTranslator().translate(msg);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertTrue(jmxBean.retryMessage(serialized));
      waitForMessages(mock, 1);
      assertEquals(1, mock.messageCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_SerializableMessage_WorkflowNotFound() throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel c = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    c.getWorkflowList().add(wf);
    adapter.getChannelList().add(c);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@BLAHBLAH");
    SerializableMessage serialized = new DefaultSerializableMessageTranslator().translate(msg);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertFalse(jmxBean.retryMessage(serialized));
      assertEquals(0, mock.messageCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_File(TestInfo info) throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel c = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    c.getWorkflowList().add(wf);
    adapter.getChannelList().add(c);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@" + getName());
    File fileToRetry = writeFile(msg, new MimeEncoder(), info);

    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertTrue(jmxBean.retryMessage(fileToRetry));
      waitForMessages(mock, 1);
      assertEquals(1, mock.messageCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_File_WorkflowNotFound(TestInfo info) throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel c = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    c.getWorkflowList().add(wf);
    adapter.getChannelList().add(c);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@BLAHBLAH");
    File fileToRetry = writeFile(msg, new MimeEncoder(), info);

    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);

      assertFalse(jmxBean.retryMessage(fileToRetry));
      assertEquals(0, mock.messageCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_File_NotEncoded(TestInfo info) throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel c = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    c.getWorkflowList().add(wf);
    adapter.getChannelList().add(c);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@BLAHBLAH");
    File fileToRetry = writeFile(msg, null, info);

    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);

      try {
        jmxBean.retryMessage(fileToRetry);
        fail();
      }
      catch (CoreException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_File_NotFoundException(TestInfo info) throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel c = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    c.getWorkflowList().add(wf);
    adapter.getChannelList().add(c);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@BLAHBLAH");
    File fileToRetry = writeFile(msg, new MimeEncoder(), info);
    fileToRetry.delete();

    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      try {
        jmxBean.retryMessage(fileToRetry);
        fail();
      }
      catch (IOException expected) {

      }
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_Retry_WorkflowNotStarted() throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel channel = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    channel.getWorkflowList().add(wf);
    adapter.getChannelList().add(channel);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    retrier.setShutdownWaitTime(new TimeInterval(10L, TimeUnit.SECONDS));
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@" + getName());
    SerializableMessage serialized = new DefaultSerializableMessageTranslator().translate(msg);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      // Close the workflow directly
      wf.requestClose();
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertFalse(jmxBean.retryMessage(serialized));
      assertEquals(0, mock.messageCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  @Test
  public void testMBean_RetryWhen_Retrier_Closed() throws Exception {
    Adapter adapter = createAdapter(getName());
    Channel channel = createChannel(getName());
    StandardWorkflow wf = createWorkflow(getName());
    MockMessageProducer mock = new MockMessageProducer();
    wf.setProducer(mock);
    channel.getWorkflowList().add(wf);
    adapter.getChannelList().add(channel);
    DefaultFailedMessageRetrier retrier = new DefaultFailedMessageRetrier();
    retrier.setShutdownWaitTime(new TimeInterval(10L, TimeUnit.SECONDS));
    adapter.setFailedMessageRetrier(retrier);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, getName() + "@" + getName());
    SerializableMessage serialized = new DefaultSerializableMessageTranslator().translate(msg);
    AdapterManager adapterManager = new AdapterManager(adapter);
    try {
      adapterManager.registerMBean();
      adapterManager.requestStart();
      // Close the workflow directly
      LifecycleHelper.stop(retrier);
      LifecycleHelper.close(retrier);
      ObjectName retrierObjName = createRetrierObjectName(adapterManager);
      DefaultFailedMessageRetrierJmxMBean jmxBean = JMX.newMBeanProxy(mBeanServer, retrierObjName,
          DefaultFailedMessageRetrierJmxMBean.class);
      assertFalse(jmxBean.retryMessage(serialized));
      assertEquals(0, mock.messageCount());
    }
    finally {
      adapterManager.requestClose();
      adapterManager.unregisterMBean();
    }
  }

  private File writeFile(AdaptrisMessage msg, MimeEncoder encoder, TestInfo info) throws IOException, CoreException {
    File result = deleteLater(msg, info);
    try (OutputStream out = new FileOutputStream(result)) {
      if (encoder != null) {
        encoder.writeMessage(msg, out);
      }
      else {
        StreamUtil.copyAndClose(msg.getInputStream(), out);
      }
    }
    return result;
  }

  private ObjectName createRetrierObjectName(AdapterManager parent) throws MalformedObjectNameException {
    return ObjectName.getInstance(JMX_FAILED_MESSAGE_RETRIER_TYPE + parent.createObjectHierarchyString() + ID_PREFIX
        + DefaultFailedMessageRetrier.class.getSimpleName());
  }
}
