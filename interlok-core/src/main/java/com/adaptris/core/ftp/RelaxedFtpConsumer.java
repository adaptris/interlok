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

package com.adaptris.core.ftp;

import static com.adaptris.core.AdaptrisMessageFactory.defaultIfNull;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * FTP implementation which does not have any guarantees as to the atomicity of operations.
 * <p>
 * This differs from the standard {@link FtpConsumer} in that it does not attempt to rename the file before attempting to process
 * the file. If you have multiple consumers reading the same directory with the same filter then it will be possible to process a
 * message twice (or twice partially) or any combination thereof. <strong>It is not recommended that you use this FtpConsumer unless
 * there are very specific reasons to; e.g. the remote FTP server does not support the RNFR and RNTO command.</strong> After
 * consuming the file, it is deleted.
 * </p>
 * <p>
 * The destination returned by the ConsumeDestination implementation should be in the form in the URL form dictated by the
 * {@link FileTransferConnection} flavour including the directory from which you wish to consume from. Simply specifying the IP
 * Address or DNS name of the remote ftp server may cause files to be consumed without specifying a subdirectory (which if you are
 * not in an ftp chroot jail might be a very bad thing).
 * </p>
 * <p>
 * Although multiple file-filters can be configured only filters that work with the filepath will work. Other filter implementations
 * (such as those based on size /last modified) may not work.
 * </p>
 * 
 * @config relaxed-ftp-consumer
 * 
 * @see FtpConnection
 * @see SftpConnection
 * @see FileTransferConnection
 * @see com.adaptris.core.ConsumeDestination
 */
@XStreamAlias("relaxed-ftp-consumer")
@AdapterComponent
@ComponentProfile(summary = "Pickup messages from an FTP/SFTP server without renaming the file first", metadata =
{
    "originalname", "fsFileSize"
}, 
    tag = "consumer,ftp,ftps,sftp", recommended = {FileTransferConnection.class})
@DisplayOrder(order =
{
    "poller", "fileFilterImp", "quietInterval", "failOnDeleteFailure"
})
public class RelaxedFtpConsumer extends FtpConsumerImpl {

  @AdvancedConfig
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #getQuietInterval()")
  private TimeInterval olderThan;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean failOnDeleteFailure;

  /**
   * Default Constructor with the following defaults.
   * <ul>
   * <li>reacquireLockBetweenMessages is true</li>
   * </ul>
   */
  public RelaxedFtpConsumer() {
    setReacquireLockBetweenMessages(true);
  }

  protected boolean fetchAndProcess(String fullPath) throws Exception {
    String filename = FtpHelper.getFilename(fullPath);
    if (additionalDebug()) {
      log.trace("Start processing [{}]", fullPath);
    }
    EncoderWrapper encWrapper = new EncoderWrapper(defaultIfNull(getMessageFactory()).newMessage(), getEncoder());
    try (EncoderWrapper wrapper = encWrapper) {
      ftpClient.get(wrapper, fullPath);
    }
    AdaptrisMessage adpMsg = addStandardMetadata(encWrapper.build(), filename);
    retrieveAdaptrisMessageListener().onAdaptrisMessage(adpMsg);
    try {
      ftpClient.delete(fullPath);
    }
    catch (Exception e) {
      if (failOnDeleteFailure()) {
        throw e;
      }
    }
    return true;
  }


  @Override
  protected long olderThanMs() {
    if (getQuietInterval() != null) {
      return getQuietInterval().toMilliseconds();
    }
    if (getOlderThan() != null) {
      log.warn("[older-than] is deprecated; use [quiet-interval] instead");
      return getOlderThan().toMilliseconds();
    }
    return DEFAULT_OLDER_THAN.toMilliseconds();
  }

  /**
   * @deprecated since 3.5.0 use {@link #getQuietInterval()} instead for consistency.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #getQuietInterval()")
  public TimeInterval getOlderThan() {
    return olderThan;
  }

  /**
   * @deprecated since 3.5.0 use {@link #setQuietInterval(TimeInterval)} instead for consistency.
   */
  @Deprecated
  @Removal(version = "3.9.0", message = "Use #setQuietInterval(TimeInterval)")
  public void setOlderThan(TimeInterval interval) {
    olderThan = interval;
  }

  private boolean failOnDeleteFailure() {
    return BooleanUtils.toBooleanDefaultIfNull(getFailOnDeleteFailure(), false);
  }

  /**
   * @return the failOnDeleteFailure
   */
  public Boolean getFailOnDeleteFailure() {
    return failOnDeleteFailure;
  }

  /**
   * Whether or not an attempt to delete the file after processing should result in an exception if it fails.
   * <p>
   * By the time the delete attempt has been made; the file has been processed by the adapter. If the delete fails (for whatever
   * reason), then it will still be possible for the adapter to re-process the file again if it exists upon the next poll trigger.
   * Setting it to be true simply allows you to record an error in the adapter log file.
   * </p>
   *
   * @param b the failOnDeleteFailure to set (default false)
   */
  public void setFailOnDeleteFailure(Boolean b) {
    failOnDeleteFailure = b;
  }

}
