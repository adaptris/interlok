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

import com.adaptris.util.text.NullConverter;
import com.adaptris.util.text.NullPassThroughConverter;

public abstract class NullableParameter extends AbstractParameter {

  private NullConverter nullConverter;

  public NullableParameter() {
    super();
  }

  public NullConverter getNullConverter() {
    return nullConverter;
  }

  /**
   * Set the {@link NullConverter} implementation to use.
   * 
   * @param nc the null converter implementation (default is {@link NullPassThroughConverter}).
   */
  public void setNullConverter(NullConverter nc) {
    this.nullConverter = nc;
  }

  NullConverter nullConverter() {
    return getNullConverter() != null ? getNullConverter() : new NullPassThroughConverter();
  }

  protected <T> T normalize(T obj) {
    return nullConverter().convert(obj);
  }
}
