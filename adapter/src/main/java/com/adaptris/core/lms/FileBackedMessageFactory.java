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

package com.adaptris.core.lms;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Message factory that returns an AdaptrisMessage Implementation that is backed by a pair of files.
 * <p>
 * In order to limit the number of temporary files that will exist in the filesystem; FileCleaningTracker is used to monitor all the
 * files that are created by temporary files. These are deleted when the underlying message is garbage collected.
 * </p>
 * 
 * @config file-backed-message-factory
 * @see FileBackedMessage
 * @see DefaultMessageFactory
 * @see AdaptrisMessageFactory
 */
@XStreamAlias("file-backed-message-factory")
@DisplayOrder(order = {"defaultCharEncoding", "tempDirectory", "maxMemorySizeBytes", "defaultBufferSize"})
public class FileBackedMessageFactory extends DefaultMessageFactory {

  // The default max size to limit a getStringPayload() to (10Mb)
  private static final long INLINE_MAX_SIZE = 1024 * 1024 * 10;
  // the default buffersize (256k) for read operations.
  private static final int DEFAULT_BUFSIZE = 1024 * 256;

  static final String TMP_FILE_SUFFIX = ".tmp";
  static final String TMP_FILE_PREFIX = "FBAM";
  private static FileCleaningTracker cleaner = new FileCleaningTracker();

  @InputFieldDefault(value = "System.getProperty(java.io.tmpdir)")
  private String tempDirectory;
  @AdvancedConfig
  @InputFieldDefault(value = "256Kb")
  private Integer defaultBufferSize;
  @AdvancedConfig
  @InputFieldDefault(value = "10Mb")
  private Long maxMemorySizeBytes;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean createTempDir;
  
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean extendedLogging;
  
  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * Default constructor.
   * <ul>
   * <li>temp-directory is based on the system property java.io.tmpdir</li>
   * <li>max-memory-size-bytes is equivalent to 10Megabytes</li>
   * <li>default-buffer-size is 256kilobytes</li>
   * </ul>
   */
  public FileBackedMessageFactory() {
    super();
  }

  /**
   * @return the tempDirectory
   */
  public String getTempDirectory() {
    return tempDirectory;
  }

  /**
   * @return the defaultBufferSize
   */
  public Integer getDefaultBufferSize() {
    return defaultBufferSize;
  }

  /**
   * Set the default buffersize to use when writing to files.
   *
   * @param bufsiz the defaultBufferSize to set
   */
  public void setDefaultBufferSize(Integer bufsiz) {
    defaultBufferSize = bufsiz;
  }

  protected int defaultBufferSize() {
    return getDefaultBufferSize() != null ? getDefaultBufferSize().intValue() : DEFAULT_BUFSIZE;
  }

  /**
   * @return the maxSizeBeforeException
   */
  public Long getMaxMemorySizeBytes() {
    return maxMemorySizeBytes;
  }

  /**
   * Set the maximum size before throwing a runtime exception when
   * {@link com.adaptris.core.AdaptrisMessage#getStringPayload()} or
   * {@link com.adaptris.core.AdaptrisMessage#getPayload()} is invoked.
   *
   * @param l the max size before exception to set
   */
  public void setMaxMemorySizeBytes(Long l) {
    maxMemorySizeBytes = l;
  }

  protected long maxMemorySizeBytes() {
    return getMaxMemorySizeBytes() != null ? getMaxMemorySizeBytes().longValue() : INLINE_MAX_SIZE;
  }


  /**
   * Set the temporary directory where files that will be used as the basis of
   * AdaptrisMessage instances wlil be created.
   *
   * @param tempDirectory the tempDirectory to set
   */
  public void setTempDirectory(String tempDirectory) {
    this.tempDirectory = tempDirectory;
  }

  protected File tempDirectory() {
    return check(getTempDirectory() != null ? new File(getTempDirectory()) : new File(System.getProperty("java.io.tmpdir")));
  }

  private File check(File f) {
    if (!f.exists() && createTempDir()) {
      f.mkdirs();
    }
    return f;
  }

  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage m = new FileBackedMessageImpl(uniqueIdGenerator(), this);
    if (!isEmpty(getDefaultCharEncoding())) {
      m.setContentEncoding(getDefaultCharEncoding());
    }
    return m;
  }

  private File createTempFile(File tempDir, Object marker) throws IOException {
    File f = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, tempDir);
    cleaner.track(f, Args.notNull(marker, "marker"), FileDeleteStrategy.FORCE);
    return f;
  }

  /**
   * Create a temp file that will be deleted when {@code marker} goes out of scope and garbage collection occurs.
   * 
   * @param marker the marker object
   * @return a temporary file.
   * @throws IOException
   */
  public File createTempFile(Object marker) throws IOException {
    return createTempFile(tempDirectory(), marker);
  }

  /**
   * @return the createTempDir
   */
  public Boolean getCreateTempDir() {
    return createTempDir;
  }

  /**
   * Specify whether or not to create any configured tempDir.
   * <p>
   * Generally speaking; if you are using the default {@code java.io.tmpdir} then you can ignore this value.
   * </p>
   * 
   * @param b whether or not to create the temporary directory; defaults to false.
   */
  public void setCreateTempDir(Boolean b) {
    this.createTempDir = b;
  }

  boolean createTempDir() {
    return BooleanUtils.toBooleanDefaultIfNull(getCreateTempDir(), false);
  }
  

  public Boolean getExtendedLogging() {
    return createTempDir;
  }

  public void setExtendedLogging(Boolean b) {
    this.createTempDir = b;
  }

  protected boolean extendedLogging() {
    return BooleanUtils.toBooleanDefaultIfNull(getExtendedLogging(), false);
  }
}
