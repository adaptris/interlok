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
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.stubs.TestBranchingService;
import com.adaptris.core.util.LifecycleHelper;

@SuppressWarnings("deprecation")
public class BranchingServiceEnablerTest extends BranchingServiceExample {

  private static final String FAIL = "fail";
  private static final String SUCCESS = "success";

  private static final String OTHER = "other";

  public BranchingServiceEnablerTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  protected void setUp() throws Exception {
  }

  public void testIsBranching() throws Exception {
    BranchingServiceEnabler s = wrap(new NullService());
    assertTrue(s.isBranching());
  }

  public void testServiceSuccess() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC");
    BranchingServiceEnabler s = wrap(new NullService());
    execute(s, msg);
    assertEquals(SUCCESS, msg.getNextServiceId());
  }

  public void testServiceFail() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC");
    BranchingServiceEnabler s = wrap(new AlwaysFailService());
    execute(s, msg);
    assertEquals(FAIL, msg.getNextServiceId());
  }

  public void testService_WrapBranching() throws Exception {
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("ABC");
    BranchingServiceEnabler s = wrap(new TestBranchingService());
    execute(s, msg);
    assertEquals("001", msg.getNextServiceId());
  }

  public void testTryInit() throws Exception {
    BranchingServiceEnabler service = new BranchingServiceEnabler();
    try {
      LifecycleHelper.init(service);
      fail("Initialised w/o a wrapped service");
    }
    catch (CoreException e) {
    }
    service = new BranchingServiceEnabler(new NullService());
    try {
      LifecycleHelper.init(service);
      fail("Initialised with null success/failure");
    }
    catch (CoreException e) {
    }
    service.setSuccessId("");
    try {
      LifecycleHelper.init(service);
      fail("Initialised with null failure");
    }
    catch (CoreException e) {
    }
    service = new BranchingServiceEnabler(new NullService());
    service.setFailureId("");
    try {
      LifecycleHelper.init(service);
      fail("Initialised with null success");
    }
    catch (CoreException e) {
    }
    service = new BranchingServiceEnabler(new NullService());
    service.setFailureId("");
    service.setSuccessId("");
    try {
      LifecycleHelper.init(service);
      assertNotNull(service.wrappedServices());
      assertEquals(1, service.wrappedServices().length);
    }
    finally {
      LifecycleHelper.close(service);
    }
  }

  private BranchingServiceEnabler wrap(Service wrapped) {
    BranchingServiceEnabler s = new BranchingServiceEnabler(wrapped);
    s.setSuccessId(SUCCESS);
    s.setFailureId(FAIL);
    return s;
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceEnabler s = new BranchingServiceEnabler(new Base64DecodeService());
    s.setFailureId(FAIL);
    s.setSuccessId(SUCCESS);

    BranchingServiceCollection sl = new BranchingServiceCollection();
    sl.addService(s);
    sl.setFirstServiceId(s.getUniqueId());
    sl.addService(new LogMessageService(FAIL));
    sl.addService(new LogMessageService(SUCCESS));
    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return BranchingServiceEnabler.class.getName();
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + "<!--"
        + "\nThis service is used to wrap other arbitrary services for use as a branch. "
        + "\nIf the service throws an exception then 'fail' is the nextServiceId."
        + "\nIf the service is successfull then 'success' is the nextServiceId."
        + "\nIn this instance, 'fail' would be nextServiceId if the payload is not base64." + "\n-->\n";
  }
}
