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

package com.adaptris.core.services.exception;


/**
 * {@link ExceptionGenerator} implementation that generates the exception from metadata.
 * 
 * @author lchan
 * 
 */
public abstract class ExceptionFromMetadataImpl implements ExceptionGenerator {

  private String exceptionMessageMetadataKey;

  public ExceptionFromMetadataImpl() {
  }

  /**
   * Returns the metadata key against which an exception message is expected at
   * runtime. <b>if a key is configured and a value is stored against it this
   * message is used instead of any configured value</b>
   *
   * @return the metadata key against which an exception message is expected at
   *         runtime
   */
  public String getExceptionMessageMetadataKey() {
    return exceptionMessageMetadataKey;
  }

  /**
   * Sets the metadata key against which an exception message is expected at
   * runtime.
   *
   * @param s the metadata key against which an exception message is expected at
   *          runtime
   */
  public void setExceptionMessageMetadataKey(String s) {
    exceptionMessageMetadataKey = s;
  }
}
