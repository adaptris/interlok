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
public class FsFilenameExistsException extends FsException {
  private static final long serialVersionUID = 2009081801L;

  public FsFilenameExistsException() { }


  public FsFilenameExistsException(Exception cause) {
    super(cause);
  }


  public FsFilenameExistsException(String description) {
    super(description);
  }

  public FsFilenameExistsException(String description, Exception cause) {
    super(description, cause);
  }
}
