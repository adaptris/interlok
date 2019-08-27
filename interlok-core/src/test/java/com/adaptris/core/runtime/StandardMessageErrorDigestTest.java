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

import static com.adaptris.core.runtime.AdapterComponentMBean.ADAPTER_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.ID_PREFIX;
import static com.adaptris.core.runtime.AdapterComponentMBean.JMX_MSG_ERR_DIGESTER_TYPE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.management.JMX;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adaptris.core.Adapter;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MessageEventGenerator;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.Workflow;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;

@SuppressWarnings("deprecation")
public class StandardMessageErrorDigestTest extends ComponentManagerCase {

  protected transient Log logR = LogFactory.getLog(this.getClass());
  private static FileCleaningTracker cleaner = new FileCleaningTracker();

  public StandardMessageErrorDigestTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testSetMaxCount() throws Exception {
    StandardMessageErrorDigester digester = createDigester();
    assertEquals(StandardMessageErrorDigester.MAX_MESSAGES, digester.getDigestMaxSize());
    digester.setDigestMaxSize(10);
    assertEquals(10, digester.getDigestMaxSize());
  }


  public void testDigest() throws Exception {
    StandardMessageErrorDigester digester = createDigester();
    File tempDir = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
    tempDir.deleteOnExit();
    cleaner.track(tempDir, digester, FileDeleteStrategy.FORCE);
    doDigesting(digester, "testDigest", tempDir.getCanonicalPath());
    assertEquals(5, digester.getDigest().size());
    assertEquals(5, digester.getTotalErrorCount());
    for (MessageDigestErrorEntry e : digester.getDigest()) {
      assertTrue(e.getWorkflowId().startsWith("workflow"));
      assertNotNull(e.getStackTrace());
      assertNotNull(e.getFileSystemPath());
      assertNotNull(e.getStackTrace());
      assertEquals(new File(tempDir, e.getUniqueId()).getCanonicalPath(), e.getFileSystemPath());
      assertNotNull(e.getLifecycleEvent());
      assertEquals(1, e.getLifecycleEvent().getMleMarkers().size());
    }
  }

  public void testDigest_NoException() throws Exception {
    StandardMessageErrorDigester digester = createDigester();
    try {
      start(digester);
      List<AdaptrisMessage> msgs = createMessages(5, 1);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      assertEquals(5, digester.getDigest().size());
      assertEquals(5, digester.getTotalErrorCount());
      for (MessageDigestErrorEntry e : digester.getDigest()) {
        assertTrue(e.getWorkflowId().startsWith("workflow"));
        assertNull(e.getStackTrace());
      }
    }
    finally {
      stop(digester);
    }
  }

  public void testAddToAdapterManager() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new StandardMessageErrorDigester(getName()));
    ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());
    AdapterManager adapterManager = new AdapterManager(adapter);
    assertTrue(adapterManager.getChildRuntimeInfoComponents().contains(digesterObj));
  }

  public void testAddToAdapterManager_NoUniqueId() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new StandardMessageErrorDigester());
    AdapterManager adapterManager = new AdapterManager(adapter);
    // The only component will be the component Checker.
    assertEquals(1, adapterManager.getChildRuntimeInfoComponents().size());
  }

  public void testMBean_JMXRegistration() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new StandardMessageErrorDigester(getName()));
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());
      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      AdapterManagerMBean adapterManager = JMX.newMBeanProxy(mBeanServer, adapterObj, AdapterManagerMBean.class);
      assertNotNull(errDigester.getParentObjectName());
      assertEquals(adapterObj, errDigester.getParentObjectName());
    }
    finally {
    }

  }

  public void testMBean_GetMessageDigest() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester();
    digester.setUniqueId(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());
      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      List<AdaptrisMessage> msgs = createMessages(5, 1);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      assertNotNull(errDigester.getDigest());
      assertEquals(5, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMBean_GetMessageDigestMultipleWorkflow() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new StandardMessageErrorDigester(getName()));
    Channel channel1 = createChannel("channel1");
    StandardWorkflow workflow1 = createWorkflow("workflow1");
    StandardWorkflow workflow2 = createWorkflow("workflow2");
    workflow1.getServiceCollection().add(new ThrowExceptionService(new ConfiguredException("fail1")));
    workflow2.getServiceCollection().add(new ThrowExceptionService(new ConfiguredException("fail2")));
    channel1.getWorkflowList().add(workflow1);
    channel1.getWorkflowList().add(workflow2);
    adapter.getChannelList().add(channel1);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);

    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());
      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);

      workflow1.onAdaptrisMessage(new DefaultMessageFactory().newMessage("Hello Workflow1"));
      workflow2.onAdaptrisMessage(new DefaultMessageFactory().newMessage("Hello Workflow2"));

      // They should have failed.
      assertNotNull(errDigester.getDigest());
      assertEquals(2, errDigester.getDigest().size());
      assertEquals(2, errDigester.getTotalErrorCount());
      MessageDigestErrorEntry entry1 = errDigester.getDigest().get(0);
      MessageDigestErrorEntry entry2 = errDigester.getDigest().get(1);
      assertEquals("workflow1@channel1", entry1.getWorkflowId());
      assertEquals("workflow2@channel1", entry2.getWorkflowId());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMBean_getDigestSubset_FromIndex() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);

    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());
      StandardMessageErrorDigesterJmxMBean errDigesterBean = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      List<AdaptrisMessage> msgs = createListOfErrors(5, 1, getName(), null);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      MessageErrorDigest digest = errDigesterBean.getDigestSubset(0);
      assertEquals(5, digest.size());
      for (MessageDigestErrorEntry e : digester.getDigest()) {
        assertTrue(e.getWorkflowId().startsWith("workflow"));
        assertNotNull(e.getStackTrace());
      }
    }
    finally {
      stop(adapter);
    }
  }

  public void testMBean_getDigestSubset_FromToIndex() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);

    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());
      StandardMessageErrorDigesterJmxMBean errDigesterBean = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      List<AdaptrisMessage> msgs = createListOfErrors(5, 1, getName(), null);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      MessageErrorDigest digest = errDigesterBean.getDigestSubset(0, 5);
      assertEquals(5, digest.size());
      for (MessageDigestErrorEntry e : digester.getDigest()) {
        assertTrue(e.getWorkflowId().startsWith("workflow"));
        assertNotNull(e.getStackTrace());
      }
    }
    finally {
      stop(adapter);

    }
  }

  public void testGetParentRuntimeInfo() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    adapter.setMessageErrorDigester(new StandardMessageErrorDigester(getName()));
    AdapterManager adapterManager = new AdapterManager(adapter);
    List<BaseComponentMBean> mBeans = new ArrayList<BaseComponentMBean>();
    mBeans.add(adapterManager);
    mBeans.addAll(adapterManager.getAllDescendants());
    try {
      register(mBeans);
      ObjectName adapterObj = adapterManager.createObjectName();
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());

      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);

      assertEquals(adapterObj, errDigester.getParentObjectName());
      assertEquals(adapterName, errDigester.getParentId());
    }
    finally {
    }
  }

  public void testMBean_RemoveDigestEntry_ByEntry() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester();
    digester.setUniqueId(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());

      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      List<AdaptrisMessage> msgs = createMessages(5, 1);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      assertNotNull(errDigester.getDigest());
      assertEquals(5, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
      errDigester.remove(new MessageDigestErrorEntry(msgs.get(0).getUniqueId(), null));
      assertEquals(4, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMBean_RemoveDigestEntry_ByEntry_WithDelete() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester();
    digester.setUniqueId(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    File tempDir = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
    tempDir.deleteOnExit();
    cleaner.track(tempDir, digester, FileDeleteStrategy.FORCE);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());

      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      // The files don't actually exist; so they won't be deleted;
      List<AdaptrisMessage> msgs = createAndDigest(digester, "testDigest", tempDir.getCanonicalPath());

      assertNotNull(errDigester.getDigest());
      assertEquals(5, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
      errDigester.remove(new MessageDigestErrorEntry(msgs.get(0).getUniqueId(), null), true);
      assertEquals(4, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMBean_RemoveDigestEntry_ByMessageId() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester();
    digester.setUniqueId(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());

      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      List<AdaptrisMessage> msgs = createMessages(5, 1);
      for (AdaptrisMessage msg : msgs) {
        digester.digest(msg);
      }
      assertNotNull(errDigester.getDigest());
      assertEquals(5, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
      errDigester.remove(msgs.get(0).getUniqueId());
      assertEquals(4, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
    }
    finally {
      stop(adapter);
    }
  }

  public void testMBean_RemoveDigestEntry_ByMessageId_WithDelete() throws Exception {
    String adapterName = this.getClass().getSimpleName() + "." + getName();
    Adapter adapter = createAdapter(adapterName);
    StandardMessageErrorDigester digester = createDigester();
    digester.setUniqueId(getName());
    adapter.setMessageErrorDigester(digester);
    List<BaseComponentMBean> mBeans = createJmxManagers(adapter);
    File tempDir = Files.createTempDirectory(this.getClass().getSimpleName()).toFile();
    tempDir.deleteOnExit();
    cleaner.track(tempDir, digester, FileDeleteStrategy.FORCE);
    try {
      start(adapter);
      register(mBeans);
      ObjectName adapterObj = createAdapterObjectName(adapterName);
      ObjectName digesterObj = createMessageErrorDigestObjectName(adapterName, getName());

      StandardMessageErrorDigesterJmxMBean errDigester = JMX.newMBeanProxy(mBeanServer, digesterObj,
          StandardMessageErrorDigesterJmxMBean.class);
      // The files don't actually exist; so they won't be deleted;
      List<AdaptrisMessage> msgs = createAndDigest(digester, "testDigest", tempDir.getCanonicalPath());

      assertNotNull(errDigester.getDigest());
      assertEquals(5, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
      errDigester.remove(msgs.get(0).getUniqueId(), true);
      assertEquals(4, errDigester.getDigest().size());
      assertEquals(5, errDigester.getTotalErrorCount());
    }
    finally {
      stop(adapter);
    }
  }

  private void doDigesting(StandardMessageErrorDigester digester, String errmsg, String fsLocation)
      throws Exception {
    try {
      start(digester);
      createAndDigest(digester, errmsg, fsLocation);
    }
    finally {
      stop(digester);
    }
  }

  private List<AdaptrisMessage> createAndDigest(StandardMessageErrorDigester digester, String errmsg, String fsLocation)
      throws Exception {
    List<AdaptrisMessage> msgs = createListOfErrors(5, 1, errmsg, fsLocation);
    for (AdaptrisMessage msg : msgs) {
      digester.digest(msg);
    }
    return msgs;
  }

  public void testTotalCount_AcrossLifecycle() throws Exception {
    StandardMessageErrorDigester digester = createDigester();
    doDigesting(digester, "testTotalCount_AcrossLifecycle", null);
    doDigesting(digester, "testTotalCount_AcrossLifecycle", null);
    assertEquals(10, digester.getDigest().size());
    assertEquals(10, digester.getTotalErrorCount());
  }


  private StandardMessageErrorDigester createDigester(String name) {
    StandardMessageErrorDigester jmx = createDigester();
    jmx.setUniqueId(name);
    return jmx;
  }

  private StandardMessageErrorDigester createDigester() {
    return new StandardMessageErrorDigester();
  }

  private List<AdaptrisMessage> createMessages(int size, int start) {
    List<AdaptrisMessage> errors = new ArrayList<AdaptrisMessage>();
    for (int i = 0; i < size; i++) {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(Workflow.WORKFLOW_ID_KEY, "workflow" + (i + start));
      errors.add(msg);
    }
    return errors;
  }

  private List<AdaptrisMessage> createListOfErrors(int size, int start, String errorMsg, String fsLocation) {
    List<AdaptrisMessage> errors = new ArrayList<AdaptrisMessage>();
    for (int i = 0; i < size; i++) {
      errors.add(createMessageWithErrors("workflow" + (i + start), errorMsg, fsLocation));
    }
    return errors;
  }

  private ObjectName createMessageErrorDigestObjectName(String adapterName, String digesterId) throws MalformedObjectNameException {
    return ObjectName.getInstance(JMX_MSG_ERR_DIGESTER_TYPE + ADAPTER_PREFIX + adapterName + ID_PREFIX
        + digesterId);

  }

  private AdaptrisMessage createMessageWithErrors(String workflowId, String errorMsg, String fsLocation) {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata(Workflow.WORKFLOW_ID_KEY, workflowId);
    msg.addMetadata(CoreConstants.PRODUCED_NAME_KEY, msg.getUniqueId());
    if (!isEmpty(fsLocation)) {
      msg.addMetadata(CoreConstants.FS_PRODUCE_DIRECTORY, fsLocation);
    }
    msg.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception(errorMsg));
    msg.addEvent(new MessageEventGenerator() {

      @Override
      public String createName() {
        return getName();
      }

      @Override
      public String createQualifier() {
        return "";
      }

      @Override
      public boolean isTrackingEndpoint() {
        return false;
      }

    }, false);
    return msg;
  }
}
