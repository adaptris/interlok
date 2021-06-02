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

package com.adaptris.core.services.conditional.operator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.services.conditional.operator.Matches;

public class MatchesTest {

  @Test
  public void testMatches() {
    Matches operator = new Matches();
    operator.setValue(".*");
    assertTrue(operator.apply(AdaptrisMessageFactory.getDefaultInstance().newMessage(), "test"));
  }
  
  @Test
  public void testNoMatch() {
    Matches operator = new Matches();
    operator.setValue("^test$");
    assertFalse(operator.apply(AdaptrisMessageFactory.getDefaultInstance().newMessage(), "xxxx"));
  }
  
  @Test
  public void testToString() {
    Matches operator = new Matches();
    operator.setValue(".*");
    assertTrue(operator.toString().contains("matches"));
  }
}
