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
import org.mockito.MockitoAnnotations;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.conditional.conditions.ConditionMetadata;
import com.adaptris.core.services.conditional.conditions.ConditionOr;
import com.adaptris.core.services.conditional.operator.IsNull;
import com.adaptris.core.services.conditional.operator.NotNull;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;

public class DoWhileTest extends ConditionalServiceExample {

  private DoWhile doWhile;

  private AdaptrisMessage message;

  private ThenService thenService;

  @Mock private Service mockService;

  @Mock private Condition mockCondition;

  private AutoCloseable openMocks;

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Before
  public void setUp() throws Exception {
    openMocks = MockitoAnnotations.openMocks(this);

    thenService = new ThenService();
    thenService.setService(mockService);

    doWhile = new DoWhile().withThen(thenService).withCondition(mockCondition);

    message = DefaultMessageFactory.getDefaultInstance().newMessage();

    LifecycleHelper.initAndStart(doWhile);

  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(doWhile);
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testShouldRunServiceOnce() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(false);

    doWhile.doService(message);

    verify(mockService, times(1)).doService(message);
  }

  @Test
  public void testShouldRunServiceMaxDefault() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);

    doWhile.doService(message);

    verify(mockService, times(10)).doService(message);
  }

  @Test
  public void testShouldRunServiceConfiguredFive() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);

    doWhile.withMaxLoops(5);
    doWhile.withOnMaxLoops((e) -> {
      return;
    });
    doWhile.doService(message);

    verify(mockService, times(5)).doService(message);
  }

  @Test
  public void testMaxLoops_ThenFail() throws Exception {
    when(mockCondition.evaluate(message)).thenReturn(true);

    doWhile.withMaxLoops(5);
    doWhile.withOnMaxLoops(new OnMaxThrowException());
    try {
      doWhile.doService(message);
    } catch (ServiceException expected) {

    }
    verify(mockService, times(5)).doService(message);
  }


  @Test
  public void testShouldRunServiceUnconfiguredFive() throws Exception {
    // 4 trues and then false to run 5 times
    when(mockCondition.evaluate(message))
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(true)
        .thenReturn(false);
    doWhile.withMaxLoops(0); // loop forever
    doWhile.doService(message);
    verify(mockService, times(5)).doService(message);
  }

  @Test
  public void testInnerServiceExceptionPropagated() throws Exception {
    when(mockCondition.evaluate(message))
        .thenReturn(true);
    doThrow(new ServiceException())
        .when(mockService)
        .doService(message);
    try {
      doWhile.doService(message);
      fail("Expected a service exception");
    } catch (ServiceException ex) {
      // expected.
    }
  }

  @Test
  public void testNoConditionSet() throws Exception {
    DoWhile doWhile = new DoWhile();
    doWhile.setCondition(null);
    try {
      doWhile.prepare();
      fail("");
    } catch (CoreException ex) {
      // expected
    }
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

    doWhile.setCondition(conditionOr);
    doWhile.setThen(thenSrvc);

    DoWhile doWhile = new DoWhile();
    doWhile.setCondition(conditionOr);
    doWhile.setThen(thenSrvc);
    return doWhile;
  }
}
