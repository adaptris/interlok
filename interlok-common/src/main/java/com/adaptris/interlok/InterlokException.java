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

package com.adaptris.interlok;



public class InterlokException extends Exception {

  private static final long serialVersionUID = 2015082101L;
  
  public InterlokException() {
    super();
  }

  public InterlokException(Throwable cause) {
    super(cause);
  }

  public InterlokException(String description) {
    super(description);
  }

  public InterlokException(String description, Throwable cause) {
    super(description, cause);
  }
}
