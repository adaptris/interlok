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

import java.util.List;

import com.adaptris.core.services.confirmation.ConfirmServiceImp;
import com.adaptris.core.stubs.LicenseStub;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.license.License;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which provides a method for marshaling sample XML config.
 * </p>
 */
public abstract class ServiceCase extends ExampleConfigCase {

  /**
   * Key in unit-test.properties that defines where example goes unless overriden {@link #setBaseDir(String)}.
   * 
   */
  public static final String BASE_DIR_KEY = "ServiceCase.baseDir";

  public ServiceCase(String name) {
    super(name);

    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected String createExampleXml(Object object) throws Exception {
    String result = getExampleCommentHeader(object);

    Service service = (Service) object;
    if (service.getUniqueId() == null || "".equals(service.getUniqueId())) {
      service.setUniqueId("unique-id");
    }

    ExampleServiceConfig config = new ExampleServiceConfig();
    config.addService(service);

    result = result + configMarshaller.marshal(config);
    return result;
  }

  public static void execute(Service s, AdaptrisMessage msg, License license) throws CoreException {
    s.isEnabled(license);
    start(s);
    try {
      s.doService(msg);
    }
    finally {
      stop(s);
    }
  }

  public static void execute(Service s, AdaptrisMessage msg) throws CoreException {
    execute(s, msg, new LicenseStub());
  }

  public static void executeWithoutStarting(Service s, AdaptrisMessage msg) throws CoreException {
    try {
      s.doService(msg);
    }
    finally {
      stop(s);
    }
  }

  public void testMessageEventGenerator() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      if (input instanceof ServiceImp) {
        assertMessageEventGenerator((ServiceImp) input);
      }
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        if (o instanceof MessageEventGenerator) {
          assertMessageEventGenerator((ServiceImp) o);
        }
      }
    }
  }

  private void assertMessageEventGenerator(ServiceImp meg) {
    meg.setIsConfirmation(null);
    assertNull(meg.getIsConfirmation());
    assertFalse(meg.isConfirmation());

    meg.setIsTrackingEndpoint(null);
    assertNull(meg.getIsTrackingEndpoint());
    assertFalse(meg.isTrackingEndpoint());

    meg.setIsConfirmation(Boolean.TRUE);
    assertNotNull(meg.getIsConfirmation());
    assertTrue(meg.isConfirmation());

    meg.setIsTrackingEndpoint(Boolean.TRUE);
    assertNotNull(meg.getIsTrackingEndpoint());
    assertTrue(meg.isTrackingEndpoint());
  }

  public void testDefaults() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertDefaults((Service) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertDefaults((Service) o);
      }
    }
  }

  /**
   * Override this method in your own service test cases, if you do not want us to try to run through the state lifecycle, testing
   * the state at each stage. For example, if you have a service which wants to connect to an application server which will of
   * course not be available during testing. In these cases attempting to init() and start() your service will throw an exception
   * and can therefore never be tested.
   */
  protected boolean doStateTests() {
    return true;
  }

  public void testServiceStates() {
    if (doStateTests()) {
      Object object = retrieveObjectForCastorRoundTrip();
      if (object instanceof Service) {
        Service service = (Service) object;

        assertEquals(service.retrieveComponentState(), ClosedState.getInstance());

        try {
          LifecycleHelper.init(service);
          assertEquals(InitialisedState.getInstance(), service.retrieveComponentState());

          try {
            LifecycleHelper.start(service);
            assertEquals(StartedState.getInstance(), service.retrieveComponentState());

            try {
              LifecycleHelper.stop(service);
              assertEquals(StoppedState.getInstance(), service.retrieveComponentState());
            }
            catch (Exception ex) {
              log.warn("Not able to test stopped state for object " + service.getClass().getSimpleName());
            }

            try {
              LifecycleHelper.close(service);
              assertEquals(ClosedState.getInstance(), service.retrieveComponentState());
            }
            catch (Exception ex) {
              log.warn("Not able to test closed state for object " + service.getClass().getSimpleName());
            }

          }
          catch (Exception ex) {
            log.warn("Not able to test started state for object " + service.getClass().getSimpleName());
          }
          catch (LinkageError ex) {
            log.warn("Not able to test started state for object " + service.getClass().getSimpleName());
          }
        }
        catch (Exception ex) {
          log.warn("Not able to test initialized state for object " + service.getClass().getSimpleName());
        }
        catch (LinkageError ex) {
          log.warn("Not able to test initialized state for object " + service.getClass().getSimpleName());
        }
      }
    }
  }

  public void testSetUniqueId() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertUniqueId((Service) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertUniqueId((Service) o);
      }
    }
  }

  public void testMessageEventGeneratorQualifier() throws Exception {
    Object input = retrieveObjectForCastorRoundTrip();
    if (input != null) {
      assertUniqueId((Service) input);
    }
    else {
      List l = retrieveObjectsForSampleConfig();
      for (Object o : retrieveObjectsForSampleConfig()) {
        assertUniqueId((Service) o);
      }
    }
  }

  public void assertMessageEventGeneratorQualifier(Service s) {
    s.setUniqueId("");
    assertEquals("", s.createQualifier());
    s.setUniqueId("MyServiceId");
    assertEquals("MyServiceId", s.createQualifier());
  }

  private void assertUniqueId(Service s) {
    try {
      s.setUniqueId(null);
      fail("null allowed for " + s.getClass());
    }
    catch (IllegalArgumentException e) {

    }
  }

  protected void assertDefaults(Service s) throws Exception {
    assertDefaults(s, true);
  }

  protected void assertDefaults(Service s, boolean assertBranching) throws Exception {
    if (s instanceof ConfirmServiceImp) {
      assertFalse(s.getClass().getName(), s.isConfirmation());
    }
    if (assertBranching) {
      if (s instanceof BranchingServiceImp) {
        assertTrue(s.getClass().getName(), s.isBranching());
      }
      else {
        assertFalse(s.getClass().getName(), s.isBranching());
      }
    }
    assertFalse(s.getClass().getName(), s.isTrackingEndpoint());
    assertFalse(s.getClass().getName(), s.continueOnFailure());
    if (s instanceof ServiceImp) {
      ((ServiceImp) s).setIsConfirmation(true);
      assertEquals(Boolean.TRUE, ((ServiceImp) s).getIsConfirmation());
      ((ServiceImp) s).setIsTrackingEndpoint(true);
      assertEquals(Boolean.TRUE, ((ServiceImp) s).getIsTrackingEndpoint());
      ((ServiceImp) s).setContinueOnFail(true);
      assertEquals(Boolean.TRUE, ((ServiceImp) s).getContinueOnFail());
    }
    if (s instanceof ServiceCollectionImp) {
      ((ServiceCollectionImp) s).setContinueOnFail(true);
      assertEquals(Boolean.TRUE, ((ServiceCollectionImp) s).getContinueOnFail());
      ((ServiceCollectionImp) s).setIsConfirmation(true);
      assertEquals(Boolean.TRUE, ((ServiceCollectionImp) s).getIsConfirmation());
      ((ServiceCollectionImp) s).setIsTrackingEndpoint(true);
      assertEquals(Boolean.TRUE, ((ServiceCollectionImp) s).getIsTrackingEndpoint());
      ((ServiceCollectionImp) s).setRestartAffectedServiceOnException(Boolean.TRUE);
      assertEquals(Boolean.TRUE, ((ServiceCollectionImp) s).getRestartAffectedServiceOnException());
      OutOfStateHandler handler = new RaiseExceptionOutOfStateHandler();
      ((ServiceCollectionImp) s).setOutOfStateHandler(handler);
      assertEquals(handler, ((ServiceCollectionImp) s).getOutOfStateHandler());
    }
  }
}
