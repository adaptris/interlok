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

package com.adaptris.core.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.util.text.NullPassThroughConverter;
import com.adaptris.util.text.NullsNotSupportedConverter;

public abstract class NullableParameterCase {


  @Test
  public void testNullConverter() throws Exception {
    NullableParameter param = createParameter();
    assertNull(param.getNullConverter());
    assertEquals(NullPassThroughConverter.class, param.nullConverter().getClass());

    param.setNullConverter(new NullsNotSupportedConverter());
    assertEquals(NullsNotSupportedConverter.class, param.getNullConverter().getClass());
    assertEquals(NullsNotSupportedConverter.class, param.nullConverter().getClass());

    param.setNullConverter(null);
    assertNull(param.getNullConverter());
    assertEquals(NullPassThroughConverter.class, param.nullConverter().getClass());

  }

  protected abstract NullableParameter createParameter();
}
