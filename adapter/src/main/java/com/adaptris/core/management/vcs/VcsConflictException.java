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

package com.adaptris.core.management.vcs;


public class VcsConflictException extends VcsException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -2380320311093071119L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public VcsConflictException() {
    super();
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous <code>Exception</code>.
   * </p>
   * 
   * @param cause a previous, causal <code>Exception</code>
   */
  public VcsConflictException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * 
   * @param description description of the <code>Exception</code>
   */
  public VcsConflictException(String description) {
    super(description);
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous <code>Exception</code> and a description
   * of the <code>Exception</code>.
   * </p>
   * 
   * @param description of the <code>Exception</code>
   * @param cause previous <code>Exception</code>
   */
  public VcsConflictException(String description, Throwable cause) {
    super(description, cause);
  }

}
