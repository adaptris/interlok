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

package com.adaptris.filetransfer;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of FileTransferClient.
 *
 */
public abstract class FileTransferClientImp implements FileTransferClient {

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());
  private transient boolean additionalDebug = false;

  public FileTransferClientImp() {
  }

  /**
   * @see FileTransferClient#dir(java.lang.String, java.io.FilenameFilter)
   */
  @Override
  public String[] dir(String directory, FilenameFilter filter)
      throws FileTransferException, IOException {
    return dir(directory, new FilenameFilterProxy(filter));
  }


  @Override
  public void setAdditionalDebug(boolean on) {
    additionalDebug = on;
  }

  protected boolean isAdditionaDebug() {
    return additionalDebug;
  }


  protected void log(String msg, Object... params) {
    if (isAdditionaDebug()) {
      logR.trace(msg, params);
    }
  }

  protected FileFilter ensureNotNull(FileFilter f) {
    return ObjectUtils.defaultIfNull(f, (file) -> {
      return true;
    });
  }

  private class FilenameFilterProxy implements FileFilter {
    private FilenameFilter proxy;

    private FilenameFilterProxy(FilenameFilter f) {
      proxy = ObjectUtils.defaultIfNull(f, (dir, name) -> {
        return true;
      });
    }

    @Override
    public boolean accept(File pathname) {
      return proxy.accept(pathname.getParentFile(), pathname.getName());
    }
  }
}
