/*
    Copyright Adaptris

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.services.conditional;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceList;
import com.adaptris.core.StartedState;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.StopProcessingService;
import com.adaptris.core.services.conditional.conditions.ConditionMetadata;
import com.adaptris.core.services.conditional.conditions.ConditionOr;
import com.adaptris.core.services.conditional.operator.IsNull;
import com.adaptris.core.services.conditional.operator.NotNull;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;

public class WhileTest extends ConditionalServiceExample {

  private While logicalExpression;

  private AdaptrisMessage message;

  private ThenService thenService;

  @Mock private Service mockService;

  @Mock private Condition mockCondition;

  private AutoCloseable openMocks = null;

  @BeforeEach
  public void setUp() throws Exception {
	openMocks = MockitoAnnotations.openMocks(this);

    when(mockService.retrieveComponentState())
        .thenReturn(StartedState.getInstance());
    
    thenService = new ThenService();
    thenService.setService(mockService);

    logicalExpression = new While().withThen(thenService).withCondition(mockCondition);

    message = DefaultMessageFactory.getDefaultInstance().newMessage();

    startMe(logicalExpression);

  }

  @AfterEach
  public void tearDown() throws Exception {
    StopMe(logicalExpression);
    Closer.closeQuietly(openMocks);
  }
  
  @Test
  public void testShouldRunServiceOnce() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true)
        .thenReturn(false);

    logicalExpression.doService(message);

    verify(mockService, times(1)).doService(message);
  }

  @Test
  public void testShouldRunServiceMaxDefault() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);

    logicalExpression.doService(message);

    verify(mockService, times(10)).doService(message);
  }

  @Test
  public void testShouldRunServiceConfiguredFive() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);

    logicalExpression.setMaxLoops(5);
    logicalExpression.doService(message);

    verify(mockService, times(5)).doService(message);
  }


  @Test
  public void testMaxLoops_ThenFail() throws Exception {
    when(mockCondition.evaluate(message)).thenReturn(true);

    logicalExpression.withMaxLoops(5).withOnMaxLoops(new OnMaxThrowException());
    try {
      logicalExpression.doService(message);
    } catch (ServiceException expected) {

    }
    verify(mockService, times(5)).doService(message);
  }

  @Test
  public void testShouldRunServiceUnconfiguredFive() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(false);

    logicalExpression.setMaxLoops(0); // loop forever
    logicalExpression.doService(message);


    verify(mockService, times(5)).doService(message);
  }

  @Test
  public void testShouldNotRunService() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(false);

    logicalExpression.doService(message);

    verify(mockService, times(0)).doService(message);
  }

  @Test
  public void testInnerServiceExceptionPropagated() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);
    doThrow(new ServiceException())
        .when(mockService)
        .doService(message);

    try {
      logicalExpression.doService(message);
      fail("Expected a service exception");
    } catch (ServiceException ex) {
      // expected.
    }
  }
  
  @Test
  public void testStopProcessingServiceCancelsLoop() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);
    
    Service stopProcessingService = new StopProcessingService();

    ServiceList services = new ServiceList();
    services.add(stopProcessingService);
    services.add(mockService);
    
    thenService = new ThenService();
    thenService.setService(services);
    
    logicalExpression = new While().withThen(thenService).withCondition(mockCondition);
    
    startMe(logicalExpression);
    
    logicalExpression.doService(message);

    // The default would loop 10 times, but the stop-processing-service should limit us to only a single loop.
    verify(mockCondition, times(1)).evaluate(message);
  }

  @Test
  public void testNoConditionSet() throws Exception {
    logicalExpression.setCondition(null);
    try {
      logicalExpression.prepare();
      fail("Expected an exception because the condition is null");
    } catch (CoreException ex) {
      // expected
    }

  }

  private void startMe(Service service) throws Exception {
    LifecycleHelper.init(service);
    LifecycleHelper.start(service);
  }

  private void StopMe(Service service) throws Exception {
    LifecycleHelper.stop(service);
    LifecycleHelper.close(service);
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    ConditionMetadata condition = new ConditionMetadata();
    condition.setMetadataKey("key1");
    condition.setOperator(new NotNull());

    ConditionMetadata condition2 = new ConditionMetadata();
    condition2.setMetadataKey("key2");
    condition2.setOperator(new IsNull());

    ConditionOr conditionOr = new ConditionOr();
    conditionOr.getConditions().add(condition);
    conditionOr.getConditions().add(condition2);

    ThenService thenSrvc = new ThenService();
    thenSrvc.setService(new LogMessageService());

    logicalExpression.setCondition(conditionOr);
    logicalExpression.setThen(thenSrvc);

    // We init and start the service in the setup, lets stop it.
    try {
      StopMe(logicalExpression);
    } catch (Exception e) {}

    return logicalExpression;
  }
}
