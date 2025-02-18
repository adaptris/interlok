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

import com.adaptris.core.metadata.RegexMetadataFilter;
import com.adaptris.core.services.exception.ConfiguredException;
import com.adaptris.core.services.exception.ThrowExceptionService;
import com.adaptris.core.services.metadata.AddMetadataService;
import com.adaptris.core.util.LifecycleHelper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CloneMessageServiceListTest
    extends com.adaptris.interlok.junit.scaffolding.services.ServiceCollectionCase {

  private static final String VAL1 = "val1";
  private static final String KEY1 = "key1";


  @Override
  public CloneMessageServiceList createServiceCollection() {
    return new CloneMessageServiceList();
  }

  @Override
  public CloneMessageServiceList createServiceCollection(Collection<Service> c) {
    return new CloneMessageServiceList(c);
  }

  private CloneMessageServiceList createServiceList() {
    CloneMessageServiceList services = createServiceCollection();
    services.addService(new AddMetadataService(Arrays.asList(new MetadataElement(KEY1, VAL1))));
    return services;
  }

  @Test
  public void testServiceOutOfStateOperation() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    try {
      service.doService(msg);
      fail("Should fail because service is not 'started'");
    } catch (ServiceException ex) {
      //expected
    }
  }

  @Test
  public void testNormalOperation() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    MarkerService marker = new MarkerService();
    service.getServices().add(marker);
    try {
      LifecycleHelper.initAndStart(service);
      service.doService(msg);

      // md not present because Service applied to a clone
      assertTrue(msg.getMetadataValue(KEY1) == null);
      assertTrue(marker.hasTriggered);
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testNewUniqueIdPerMessage() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    Set<String> messageUniqueIds = new LinkedHashSet<>();
    CloneMessageServiceList service1 = createServiceList();
    service1.setNewUniqueIdPerMessage(false);
    FunctionService functionService = new FunctionService((m) -> {
        messageUniqueIds.add(m.getUniqueId());
        return null;
    });
    service1.getServices().add(functionService);
    service1.getServices().add(functionService);

    CloneMessageServiceList service2 = createServiceList();
    service2.setNewUniqueIdPerMessage(true);
    service2.getServices().add(functionService);
    service2.getServices().add(functionService);

    try {
      LifecycleHelper.initAndStart(service1);
      service1.doService(msg);
      assertEquals(1, messageUniqueIds.size());
      assertTrue(messageUniqueIds.contains(msg.getUniqueId()));

      messageUniqueIds.clear();

      LifecycleHelper.initAndStart(service2);
      service2.doService(msg);
      assertEquals(2, messageUniqueIds.size());
      assertFalse(messageUniqueIds.contains(msg.getUniqueId()));

    }
    finally {
      LifecycleHelper.stopAndClose(service1);
      LifecycleHelper.stopAndClose(service2);
    }
  }


  @Test
  public void testHaltProcessing() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    MarkerService marker = new MarkerService();
    service.getServices().add(marker);
    try {
      LifecycleHelper.initAndStart(service);
      msg.addMetadata(CoreConstants.STOP_PROCESSING_KEY, CoreConstants.STOP_PROCESSING_VALUE);
      service.doService(msg);

      // md not present because Service applied to a clone
      assertTrue(msg.getMetadataValue(KEY1) == null);
      assertFalse(marker.hasTriggered);
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testNormalOperationPreserveKey() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    CloneMessageServiceList service = createServiceList();
    RegexMetadataFilter rmf = new RegexMetadataFilter();
    rmf.addIncludePattern(KEY1);
    service.setOverrideMetadataFilter(rmf);
    try {
      LifecycleHelper.initAndStart(service);

      service.doService(msg);

      // md not present because Service applied to a clone
      assertNotNull(msg.getMetadataValue(KEY1));
      assertEquals(VAL1, msg.getMetadataValue(KEY1));
    }
    finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testFailWithNoContinueOnFail() throws Exception {
    CloneMessageServiceList services = createServiceList();
    services.addService(new ThrowExceptionService(new ConfiguredException("Fail")));
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (Exception e) {
      // expected...
    }
  }

  @Test
  public void testFailWithContinueOnFail() throws Exception {
    CloneMessageServiceList services = createServiceList();
    ServiceImp service3 = new ThrowExceptionService(new ConfiguredException("Fail"));
    service3.setContinueOnFail(true);
    services.addService(service3);
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    execute(services, msg);
  }

  @Test
  public void testRestartAffectedServiceOnFail() throws Exception {
    CloneMessageServiceList services = createServiceList();
    services.setRestartAffectedServiceOnException(Boolean.TRUE);
    FailWithInitCount service = new FailWithInitCount();
    services.addService(service);

    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();

    try {
      execute(services, msg);
      fail("Expected Service Exception");
    }
    catch (Exception e) {
      assertEquals(2, service.getInitCount());
      // expected...
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    CloneMessageServiceList result = new CloneMessageServiceList();
    result.addService(new NullService());
    result.addService(new NullService());
    result.addService(new NullService());
    return result;
  }

  protected Class marshalledClassName() {
    return CloneMessageServiceList.class;
  }

  private class FailWithInitCount extends ThrowExceptionService {
    private int initCount = 0;

    FailWithInitCount() {
      super(new ConfiguredException("Fail"));
    }

    @Override
    protected void initService() throws CoreException {
      initCount++;
      super.initService();
    }

    int getInitCount() {
      return initCount;
    }
  }

  private class FunctionService extends ServiceImp {

    private transient Function<AdaptrisMessage, Void> function;

    public FunctionService(Function<AdaptrisMessage, Void> function) {
      this.function = function;
    }

    public FunctionService(Function<AdaptrisMessage, Void> function, String s) {
      this(function);
      setUniqueId(s);
    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      function.apply(msg);
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
      function = null;
    }

    @Override
    public void prepare() throws CoreException {
    }

  }

  private class MarkerService extends ServiceImp {

    private transient boolean hasTriggered = false;

    public MarkerService() {
    }

    public MarkerService(String s) {
      this();
      setUniqueId(s);
    }

    @Override
    public void doService(AdaptrisMessage msg) throws ServiceException {
      hasTriggered = true;
    }

    @Override
    protected void initService() throws CoreException {
    }

    @Override
    protected void closeService() {
      hasTriggered = false;
    }

    @Override
    public void prepare() throws CoreException {
    }

  }

}
