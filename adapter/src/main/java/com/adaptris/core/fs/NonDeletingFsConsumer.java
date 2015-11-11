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

import org.perf4j.aop.Profiled;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
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
 * The configured <code>Destination</code> may return a string in one of two formats
 * </p>
 * <ul>
 * <li>If a <code>file</code> based url is used. e.g. file:///c:/path/to/my/directory or file:////path/to/my/directory then the
 * patch is considered to be fully qualified</li>
 * <li>If just a path is returned, then it is considered to be relative to the current working directory. e.g. if /opt/fred is used,
 * and the adapter is installed to /opt/adapter, then the fully qualified name is /opt/adapter/opt/fred.</li>
 * </ul>
 * <p>
 * On windows based platforms, you should always use a file based url.
 * </p>
 * 
 * @config non-deleting-fs-consumer
 * 
 */
@XStreamAlias("non-deleting-fs-consumer")
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

  public NonDeletingFsConsumer(ConsumeDestination d) {
    this();
    setDestination(d);
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
  @Profiled(tag = "{$this.getClass().getSimpleName()}.processFile()", logger = "com.adaptris.perf4j.fs.TimingLogger")
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
            this.retrieveAdaptrisMessageListener().onAdaptrisMessage(msg);
            result++;
          }
          else {
            log.trace("[" + item.getAbsolutePath() + "] hasn't changed since last poll");
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

  @Override
  protected void prepareConsumer() throws CoreException {
    getProcessedItemCache().prepare();
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
    this.processedItemCache = cache;
  }

  private boolean hasChanged(ProcessedItem entry) throws Exception {
    boolean result = false;
    log.trace("Checking cache for [" + entry.getAbsolutePath() + "]");
    ProcessedItem cachedEntry = getProcessedItemCache().get(entry.getAbsolutePath());
    if (cachedEntry == null) {
      log.trace("[" + entry.getAbsolutePath() + "] not in cache");
      result = true;
    }
    else {
      log.trace("[" + entry.getAbsolutePath() + "] found");
      if (entry.getFilesize() != cachedEntry.getFilesize()) {
        log.trace("[" + entry.getAbsolutePath() + "] filesize has changed");
        result = true;
      }
      else if (entry.getLastModified() != cachedEntry.getLastModified()) {
        log.trace("[" + entry.getAbsolutePath() + "] lastmodified has changed");
        result = true;
      }
    }
    return result;
  }
}
