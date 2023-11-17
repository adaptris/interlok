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

package com.adaptris.core.fs;

import java.io.File;
import java.io.FileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link FileFilter} that accepts files that are files that just uses
 * {@code org.apache.commons.io.filefilter.FileFileFilter} under the covers.
 */
@Slf4j
public class IsFileFilter implements FileFilter {

  public IsFileFilter() {
  }

  @Override
  public boolean accept(File pathname) {
    return FileFileFilter.INSTANCE.accept(pathname);
  }

}
