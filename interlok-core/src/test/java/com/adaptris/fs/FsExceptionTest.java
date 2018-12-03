/*
 * Copyright 2018 Adaptris Ltd.
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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class FsExceptionTest {

  @Test
  public void testFsException() {
    assertNotNull(new FsException());
    assertNotNull(new FsException(new Exception()));
    assertNotNull(new FsException("hello"));
    assertNotNull(new FsException("hello", new Exception()));
  }

  @Test
  public void testFsFilenameExistsException() {
    assertNotNull(new FsFilenameExistsException());
    assertNotNull(new FsFilenameExistsException(new Exception()));
    assertNotNull(new FsFilenameExistsException("hello"));
    assertNotNull(new FsFilenameExistsException("hello", new Exception()));
  }

  @Test
  public void testFsFileNotFoundException() {
    assertNotNull(new FsFileNotFoundException());
    assertNotNull(new FsFileNotFoundException(new Exception()));
    assertNotNull(new FsFileNotFoundException("hello"));
    assertNotNull(new FsFileNotFoundException("hello", new Exception()));
  }
}
