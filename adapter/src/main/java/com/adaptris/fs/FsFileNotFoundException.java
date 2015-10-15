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

package com.adaptris.fs;


/**
 * <p>
 * Subclass of <code>FsException</code> indicating that a file has not be found.
 * </p>
 */
public class FsFileNotFoundException extends FsException {
  /**
   * 
   */
  private static final long serialVersionUID = 2009081801L;
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public FsFileNotFoundException() { }

  
  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public FsFileNotFoundException(Exception cause) {
    super(cause);
  }
  
  
  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description of the <code>Exception</code>
   */
  public FsFileNotFoundException(String description) {
    super(description);
  }
  
    
  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code> and a description of the <code>Exception</code>.
   * </p>
   * @param description of the <code>Exception</code>
   * @param cause previous <code>Exception</code>
   */
  public FsFileNotFoundException(String description, Exception cause) {
    super(description, cause);
  }
} 
