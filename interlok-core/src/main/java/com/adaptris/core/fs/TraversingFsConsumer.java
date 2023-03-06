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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.fs.FsException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Extension of the standard FsConsumer that traverses all subdirectories for files that match the filter expression.
 * </p>
 *
 * @config traversing-fs-consumer
 *
 */
@JacksonXmlRootElement(localName = "traversing-fs-consumer")
@XStreamAlias("traversing-fs-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from the filesystem; traversing sub-directories", tag = "consumer,fs,filesystem", metadata =
{
        CoreConstants.ORIGINAL_NAME_KEY, CoreConstants.FS_FILE_SIZE,
        CoreConstants.FILE_LAST_MODIFIED_KEY, CoreConstants.FS_CONSUME_DIRECTORY,
        CoreConstants.MESSAGE_CONSUME_LOCATION, CoreConstants.FS_CONSUME_PARENT_DIR
},
    recommended = {NullConnection.class})
@DisplayOrder(order = {"baseDirectoryUrl", "poller", "createDirs", "filterExpression",
    "fileFilterImp", "fileSorter", "wipSuffix", "resetWipFiles"})
public class TraversingFsConsumer extends FsConsumer {

  public TraversingFsConsumer() {
    super();
  }

  @Override
  protected int processMessages() {
    List<File> fileList = new ArrayList<File>();
    int filesProcessed = 0;
    try {
      File dir = verifyDirectory();
      fileList = Arrays.asList(dir.listFiles());
    }
    catch (Exception e) {
      log.warn("Exception listing files in [{}], waiting for next scheduled poll", baseDirUrl());
      if (logAllExceptions()) {
        log.warn(e.getMessage(), e);
      }
      return 0;
    }
    fileList = getFileSorter().sort(fileList);
    for (File file : fileList) {
      try {
        filesProcessed += processFile(file);
        if (!continueProcessingMessages(filesProcessed)) {
          break;
        }

      }
      catch (Exception e) {
        log.warn("Exception processing [{}], waiting for next scheduled poll", file.getName());
        if (logAllExceptions()) {
          log.warn(e.getMessage(), e);
        }
      }
    }
    return filesProcessed;
  }

  @Override
  protected int processFile(File f) throws CoreException {
    int result = 0;
    logFile(f, "processFile ");
    if (f.isDirectory()) {
      result += processDirectory(f);
    }
    else {
      // Can't use super.processFile() here because
      // we need to preserve the "directory name" from which we read the stuff.
      result += handleFile(f);
    }
    return result;
  }

  private int processDirectory(File srcDir) throws CoreException {
    int result = 0;
    logFile(srcDir, "processDirectory ");
    List<File> fileList = Arrays.asList(srcDir.listFiles());
    fileList = getFileSorter().sort(fileList);

    for (File f : fileList) {
      result += processFile(f);
    }
    return result;
  }

  private void logFile(File f, String prefix) {
    // try {
    // log.trace("{} {}", prefix, f.getCanonicalPath());
    // }
    // catch (IOException e) {
    //
    // }

  }

  private int handleFile(File originalFile) throws CoreException {
    int rc = 0;
    logFile(originalFile, "handleFile ");

    try {
      if (originalFile.getName().endsWith(getWipSuffix())) {
        log.debug("ignoring part-processed file [{}]", originalFile.getName());
      }
      else if (fileFilter != null && !fileFilter.accept(originalFile)) {
        log.trace("File [{}] doesn't match filter", originalFile.getName());
      }
      else {
        if (checkModified(originalFile) && isFileAccessible(originalFile)) {
          File wipFile = renameFile(originalFile);
          AdaptrisMessage msg = createAdaptrisMessage(wipFile);
          addStandardMetadata(msg, originalFile, wipFile);
          retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
          fsWorker.delete(wipFile);
          rc++;
        }
        else {
          log.trace("[{}] not deemed safe to process", originalFile.getName());
        }
      }
    }
    catch (FsException e) {
      throw new CoreException(e);
    }
    catch (IOException e) {
      throw new CoreException(e);
    }
    return rc;
  }
}
