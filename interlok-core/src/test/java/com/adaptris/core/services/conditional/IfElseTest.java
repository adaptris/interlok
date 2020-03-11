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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.NullService;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.conditional.conditions.ConditionAnd;
import com.adaptris.core.services.conditional.conditions.ConditionExpression;
import com.adaptris.core.services.conditional.conditions.ConditionFunction;
import com.adaptris.core.services.conditional.conditions.ConditionMetadata;
import com.adaptris.core.services.conditional.conditions.ConditionOr;
import com.adaptris.core.services.conditional.operator.Equals;
import com.adaptris.core.services.conditional.operator.NotNull;
import com.adaptris.core.services.routing.AlwaysMatchSyntaxIdentifier;
import com.adaptris.core.util.LifecycleHelper;

public class IfElseTest extends ConditionalServiceExample {

  private IfElse logicalExpression;

  private AdaptrisMessage message;
  
  private ThenService thenService;
  
  private ElseService elseService;
  
  @Mock private Service mockService;
  
  @Mock private Service mockElseService;
  
  @Mock private Condition mockCondition;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    thenService = new ThenService();
    elseService = new ElseService();
    
    thenService.setService(mockService);
    elseService.setService(mockElseService);
    
    logicalExpression = new IfElse();
    logicalExpression.setThen(thenService);
    logicalExpression.setOtherwise(elseService);
    logicalExpression.setCondition(mockCondition);
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    LifecycleHelper.initAndStart(logicalExpression);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(logicalExpression);
  }

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Test
  public void testNoThenService() throws Exception {
    logicalExpression.getThen().setService(new NullService());
    
    when(mockCondition.evaluate(message))
        .thenReturn(true);

    // purely to re-initiate the NoOpService
    LifecycleHelper.stopAndClose(logicalExpression);
    LifecycleHelper.initAndStart(logicalExpression);
    
    logicalExpression.doService(message);
    
    verify(mockElseService, times(0)).doService(message);
  }

  @Test
  public void testNoElseService() throws Exception {
    logicalExpression.getOtherwise().setService(new NullService());
    
    when(mockCondition.evaluate(message))
        .thenReturn(true);

    // purely to re-initiate the NoOpService
    LifecycleHelper.stopAndClose(logicalExpression);
    LifecycleHelper.initAndStart(logicalExpression);
    
    logicalExpression.doService(message);
    
    verify(mockService, times(1)).doService(message);
  }

  @Test
  public void testShouldRunService() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);
    
    logicalExpression.doService(message);
    
    verify(mockService).doService(message);
  }

  @Test
  public void testShouldNotRunService() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(false);
    
    logicalExpression.doService(message);
    
    verify(mockService, times(0)).doService(message);
    verify(mockElseService, times(1)).doService(message);
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
  public void testNoConditionSet() throws Exception {
    try {
      LifecycleHelper.initAndStart(new IfElse());
      fail("Expected an exception because the condition is null");
    } catch (CoreException ex) {
      // expected
    }
    
  }


  @Test
  public void testSyntaxIdentifier() throws Exception {
    Service mockThen = Mockito.mock(Service.class);
    Service mockElse = Mockito.mock(Service.class);
    IfElse ifElse = new IfElse();
    ifElse.getThen().setService(mockThen);
    ifElse.getOtherwise().setService(mockElse);
    ifElse.setCondition(new AlwaysMatchSyntaxIdentifier());
    try {
      LifecycleHelper.initAndStart(ifElse);
      ifElse.doService(message);
      verify(mockThen, times(1)).doService(message);
      verify(mockElse, times(0)).doService(message);
    } finally {
      LifecycleHelper.stopAndClose(ifElse);
    }
  }


  @Override
  protected Object retrieveObjectForSampleConfig() {
    IfElse result = new IfElse();
    ConditionMetadata condition = new ConditionMetadata();
    condition.setMetadataKey("key1");
    condition.setOperator(new NotNull());
    
    Equals equals = new Equals();
    equals.setValue("myValue");
    ConditionMetadata condition2 = new ConditionMetadata();
    condition2.setMetadataKey("key2");
    condition2.setOperator(equals);
    
    ConditionOr conditionOr = new ConditionOr();
    ConditionExpression conditionExpression = new ConditionExpression();
    conditionExpression.setAlgorithm("(%message{key1} - 10) == %message{key2}");
    conditionOr.getConditions().add(conditionExpression);
    conditionOr.getConditions().add(condition2);
    
    ConditionAnd conditionAnd = new ConditionAnd();
    conditionAnd.getConditions().add(condition);
    conditionAnd.getConditions().add(conditionOr);
    conditionAnd.getConditions().add(
        new ConditionFunction("function evaluateScript(message) { return message.getMetadataValue('mykey').equals('myvalue');}"));
    ThenService thenSrvc = new ThenService();
    ElseService elseSrvc = new ElseService();
    
    thenSrvc.setService(new LogMessageService());
    elseSrvc.setService(new LogMessageService());
    
    result.setCondition(conditionAnd);
    result.setThen(thenSrvc);
    result.setOtherwise(elseSrvc);

    
    return result;
  }
}
