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

package com.adaptris.zz.cleanup;

import org.junit.Test;

import com.adaptris.core.lms.FilePurge;

// It appears to be the case that FileCleaningTracker
// doesn't appear to work within junit...
// WE don't want loads of temporary files hanging around
// This should be the last case that's run.
public class FileCleanupTest {

  @Test
  public void testCleanupTemporaryFiles() {
    System.gc();
    FilePurge.getInstance().purge();
  }

}
