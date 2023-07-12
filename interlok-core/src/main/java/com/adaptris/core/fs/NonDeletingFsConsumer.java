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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * File system implementation of <code>AdaptrisMessageConsumer</code> based on the <code>com.adaptris.fs</code> package.
 * </p>
 * <p>
 * This differs from the standard implementation of {@linkplain FsConsumer} in that it processes the file 'in-situ' and does not
 * delete the file after processing. To avoid constantly re-processing the same file over and over again, you can configure a
 * {@linkplain ProcessedItemCache} which stores the last modified timestamp of the file and only re-processes the file if it
 * changes.
 * </p>
 * <p>
 * The configured <code>Base Directory URL</code> may return a string in one of two formats
 * </p>
 * <ul>
 * <li>
 * Supports URLs with both the {@code file scheme} and without.
 * </li>
 * <li>
 * If you define a directory without any leading slash or 
 * if it starts with a slash is deemed to be an <strong>absolute</strong> path.
 * </li>
 * <li>
 * If "./" or "../" is used at the start of your definition then
 * the path is deemed to be <strong>relative</strong>.</li>
 * <li>
 * This is true whether using the {@code file scheme} or not.
 * </li>
 * <li>
 * With Windows systems the above is above is true, plus if you simply define the <strong>absolute</strong> path including the drive letter
 * e.g. 'c://my/path' this is also valid.
 * </li>
 * <li>
 * Both / and \ slashes are supported.
 * </li>
 * </ul>
 *
 * @config non-deleting-fs-consumer
 *
 */
@XStreamAlias("non-deleting-fs-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from the filesystem without deleting them afterwards", tag = "consumer,fs,filesystem", metadata =
{
        CoreConstants.ORIGINAL_NAME_KEY, CoreConstants.FS_FILE_SIZE,
        CoreConstants.FILE_LAST_MODIFIED_KEY, CoreConstants.FS_CONSUME_DIRECTORY,
        CoreConstants.MESSAGE_CONSUME_LOCATION, CoreConstants.FS_CONSUME_PARENT_DIR
}, recommended =
{
    NullConnection.class
})
@DisplayOrder(order = {"baseDirectoryUrl", "poller", "createDirs", "filterExpression",
    "fileFilterImp", "fileSorter", "processedItemCache"})
public class NonDeletingFsConsumer extends FsConsumerImpl {

  @NotNull
  @Valid
  @AutoPopulated
  @AdvancedConfig
  private ProcessedItemCache processedItemCache;
  private transient ProcessedItemList filesDetected;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public NonDeletingFsConsumer() {
    super();
    setProcessedItemCache(new InlineItemCache());
    filesDetected = new ProcessedItemList();
  }

  @Override
  protected int processMessages() {
    int result = super.processMessages();
    getProcessedItemCache().update(filesDetected);
    getProcessedItemCache().evict();
    getProcessedItemCache().save();
    filesDetected.getProcessedItems().clear();
    return result;
  }

  @Override
  protected int processFile(File fileToProcess) throws CoreException {
    int result = 0;
    try {
      if (checkModified(fileToProcess) && isFileAccessible(fileToProcess)) {
        ProcessedItem item = new ProcessedItem(fileToProcess.getCanonicalPath(), fileToProcess.lastModified(), fileToProcess
            .length());
        try {
          filesDetected.addProcessedItem(item);
          if (hasChanged(item)) {
            AdaptrisMessage msg = createAdaptrisMessage(fileToProcess);
            addStandardMetadata(msg, fileToProcess, fileToProcess);
            retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
            result++;
          }
          else {
            log.trace("[{}] hasn't changed since last poll", item.getAbsolutePath());
          }
        }
        catch (Exception e) {
          filesDetected.removeProcessedItem(item);
          throw e;
        }
      }
    }
    catch (Exception e) {
      throw new CoreException(e);
    }
    return result;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
    super.init();
    filesDetected.getProcessedItems().clear();
  }

  @Override
  public void start() throws CoreException {
    super.start();
  }

  @Override
  public void close() {
    super.close();
  }

  @Override
  public void stop() {
    super.stop();
  }

  public ProcessedItemCache getProcessedItemCache() {
    return processedItemCache;
  }

  public void setProcessedItemCache(ProcessedItemCache cache) {
    processedItemCache = cache;
  }

  protected boolean hasChanged(ProcessedItem entry) throws Exception {
    boolean result = false;
    log.trace("Checking cache for [{}]", entry.getAbsolutePath());
    ProcessedItem cachedEntry = getProcessedItemCache().get(entry.getAbsolutePath());
    if (cachedEntry == null) {
      log.trace("[{}] not in cache", entry.getAbsolutePath());
      result = true;
    }
    else {
      // log.trace("[{}] found", entry.getAbsolutePath());
      if (entry.getFilesize() != cachedEntry.getFilesize()) {
        log.trace("[{}] filesize has changed", entry.getAbsolutePath());;
        result = true;
      }
      else if (entry.getLastModified() != cachedEntry.getLastModified()) {
        log.trace("[{}]", entry.getAbsolutePath());
        result = true;
      }
    }
    return result;
  }
}
