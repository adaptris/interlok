/*
 * Copyright 2017 Adaptris Ltd.
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
package com.adaptris.jdbc;

import java.sql.Statement;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.InputFieldDefault;

public abstract class CallableStatementExecutorImpl implements CallableStatementExecutor {

  @InputFieldDefault(value = "false")
  private Boolean handleMultipleResultsetsQuietly;

  public CallableStatementExecutorImpl() {

  }

  public Boolean getHandleMultipleResultsetsQuietly() {
    return handleMultipleResultsetsQuietly;
  }

  /**
   * Ignore SQL Exceptions when calling {@link Statement#getMoreResults(int)}.
   * 
   * @param b true to ignore exceptions from {@link Statement#getMoreResults(int)}; default null (false).
   */
  public void setHandleMultipleResultsetsQuietly(Boolean b) {
    this.handleMultipleResultsetsQuietly = b;
  }

  protected boolean ignoreMoreResultsException() {
    return BooleanUtils.toBooleanDefaultIfNull(getHandleMultipleResultsetsQuietly(), false);
  }
}
