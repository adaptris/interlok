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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.services.conditional.Condition;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.util.Closer;

public class ConditionOrTest {

  private ConditionOr conditionOr;

  private AdaptrisMessage adaptrisMessage;

  @Mock private Condition mockConditionOne;

  @Mock private Condition mockConditionTwo;

  @Mock private Condition mockConditionThree;

  private AutoCloseable openMocks = null;

  @BeforeEach
  public void setUp() throws Exception {
	openMocks = MockitoAnnotations.openMocks(this);

    conditionOr = new ConditionOr();
    conditionOr.getConditions().add(mockConditionOne);
    conditionOr.getConditions().add(mockConditionTwo);
    conditionOr.getConditions().add(mockConditionThree);

    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    LifecycleHelper.initAndStart(conditionOr);
  }

  @AfterEach
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(conditionOr);
    Closer.closeQuietly(openMocks);
  }

  @Test
  public void testOrAllTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(true);

    assertTrue(conditionOr.evaluate(adaptrisMessage));

    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo, times(0)).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }

  @Test
  public void testOrAllFalse() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(false);

    assertFalse(conditionOr.evaluate(adaptrisMessage));

    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo).evaluate(adaptrisMessage);
    verify(mockConditionThree).evaluate(adaptrisMessage);
  }

  @Test
  public void testOrLastConditionTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(true);

    assertTrue(conditionOr.evaluate(adaptrisMessage));

    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo).evaluate(adaptrisMessage);
    verify(mockConditionThree).evaluate(adaptrisMessage);
  }

  @Test
  public void testOrFirstConditionTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(false);

    assertTrue(conditionOr.evaluate(adaptrisMessage));

    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo, times(0)).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }

  @Test
  public void testOrMiddleConditionTrue() throws Exception {
    when(mockConditionOne.evaluate(adaptrisMessage)).thenReturn(false);
    when(mockConditionTwo.evaluate(adaptrisMessage)).thenReturn(true);
    when(mockConditionThree.evaluate(adaptrisMessage)).thenReturn(false);

    assertTrue(conditionOr.evaluate(adaptrisMessage));

    verify(mockConditionOne).evaluate(adaptrisMessage);
    verify(mockConditionTwo).evaluate(adaptrisMessage);
    verify(mockConditionThree, times(0)).evaluate(adaptrisMessage);
  }

  @Test
  public void testNoConditionsSet() throws Exception {
    conditionOr.getConditions().clear();
    assertFalse(conditionOr.evaluate(adaptrisMessage));
  }
}
