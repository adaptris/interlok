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

package com.adaptris.core.services.conditional.conditions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.services.conditional.conditions.ConditionAnd;
import com.adaptris.core.services.conditional.conditions.ConditionImpl;
import com.adaptris.core.util.LifecycleHelper;

public class ConditionAndTest {

private ConditionAnd conditionAnd;
  
  private AdaptrisMessage adaptrisMessage;
  
  @Mock private Condition mockConditionOne;
  
  @Mock private Condition mockConditionTwo;
  
  @Mock private Condition mockConditionThree;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    conditionAnd = new ConditionAnd().withConditions(mockConditionOne, mockConditionTwo, mockConditionThree);
    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    LifecycleHelper.initAndStart(conditionAnd);
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(conditionAnd);
  }

  @Test
  public void testAndAllTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(true);
    
    assertTrue(conditionAnd.evaluate(adaptrisMessage));
    
    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo).evaluate(adaptrisMessage);
    verify(mockConditionThree).evaluate(adaptrisMessage);
  }
  
  @Test
  public void testAndAllFalse() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(false);
    
    assertFalse(conditionAnd.evaluate(adaptrisMessage));
    
    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo, times(0)).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }
  
  @Test
  public void testAndLastConditionTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(true);
    
    assertFalse(conditionAnd.evaluate(adaptrisMessage));
    
    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo, times(0)).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }
  
  @Test
  public void testAndFirstConditionTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(false);
    
    assertFalse(conditionAnd.evaluate(adaptrisMessage));
    
    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }
  
  @Test
  public void testAndMiddleConditionTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(false);
    
    assertFalse(conditionAnd.evaluate(adaptrisMessage));
    
    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo, times(0)).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }
  
  @Test
  public void testAndNoConditionsSet() throws Exception {
    conditionAnd.getConditions().clear();
    assertFalse(conditionAnd.evaluate(adaptrisMessage));
  }

  @Test
  public void testLifecycle_BrokenCondition() throws Exception {
    ConditionAnd condition = new ConditionAnd();
    condition.withConditions(new MyCondition());
    LifecycleHelper.initAndStart(condition);
    LifecycleHelper.stopAndClose(condition);
  }

  private class MyCondition extends ConditionImpl {

    @Override
    public boolean evaluate(AdaptrisMessage message) throws CoreException {
      return false;
    }

    public void close() {
      throw new RuntimeException();
    }
  }
}
