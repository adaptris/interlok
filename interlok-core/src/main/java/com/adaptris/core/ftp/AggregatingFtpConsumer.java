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

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.ServiceException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.services.aggregator.AggregatingConsumerImpl;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.filetransfer.FileTransferClient;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.apache.commons.lang3.BooleanUtils;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.adaptris.core.ftp.FtpHelper.FORWARD_SLASH;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * {@link com.adaptris.core.services.aggregator.AggregatingConsumer} implementation that allows you to read a separate message from
 * an FTP filesystem that is correlated in some way to the current message.
 * <p>
 * If a filter-expression of the generated is available, then this works as a true aggregator; it will
 * trigger the use of a FileFilter, and ultimately cause multiple files to be read and passed to the configured message aggregator.
 * Note that no decision is made about the resulting size of the message, all messages that match the filter-expression will be
 * aggregated; if there a 2000 files sitting on the filesystem that match the filter-expression, then that is how many will be
 * picked up.
 * </p>
 * 
 * @config aggregating-ftp-consumer
 * 
 */
@XStreamAlias("aggregating-ftp-consumer")
@DisplayOrder(order =
{
    "destination", "messageAggregator", "filterFilterImp", "deleteAggregatedFiles", "encoder"
})
public class AggregatingFtpConsumer extends AggregatingConsumerImpl<AggregatingFtpConsumeService> {
  private static final String DEFAULT_FILE_FILTER_IMP = "org.apache.commons.io.filefilter.RegexFileFilter";
  private static final String OBJ_METADATA_KEY_FILENAME = AggregatingFtpConsumer.class.getCanonicalName() + ".filename";

  @InputFieldHint(ofType = "java.io.FileFilter")
  @AdvancedConfig
  private String fileFilterImp;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean deleteAggregatedFiles;
  @Valid
  @AdvancedConfig
  private AdaptrisMessageEncoder encoder;

  @Override
  public void aggregateMessages(AdaptrisMessage msg, AggregatingFtpConsumeService service) throws ServiceException {

    String endpoint = getEndpoint();
    if (endpoint != null) {
      endpoint = msg.resolveObject(endpoint).toString();
    }
    String filterExpression = getFilterExpression();
    if (filterExpression != null) {
      filterExpression = msg.resolveObject(filterExpression).toString();
    }

    ConfigWrapper cfg = new ConfigWrapper(service.getConnection().retrieveConnection(FileTransferConnection.class), endpoint, filterExpression);
    try {
      List<AdaptrisMessage> result = isEmpty(cfg.filterExpression) ? single(cfg, msg.getFactory()) : multiple(cfg, msg.getFactory());
      getMessageAggregator().joinMessage(msg, result);
      if (deleteAggregatedFiles()) {
        deleteFilesQuietly(result, cfg);
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }

  }

  private void deleteFilesQuietly(List<AdaptrisMessage> msgs, ConfigWrapper cfg) throws Exception {
    FileTransferClient ftpClient = cfg.remote.connect(cfg.endpoint);
    for (AdaptrisMessage msg : msgs) {
      String file = (String) msg.getObjectHeaders().get(OBJ_METADATA_KEY_FILENAME);
      try {
        ftpClient.delete(file);
      }
      catch (Exception e) {
        if (cfg.remote.additionalDebug()) {
          log.trace("Failed to delete [{}]", file);
        }
      }
    }
    cfg.remote.disconnect(ftpClient);
  }

  private List<AdaptrisMessage> multiple(ConfigWrapper cfg, AdaptrisMessageFactory factory) throws Exception {
    FileTransferClient ftpClient = cfg.remote.connect(cfg.endpoint);
    String pollDirectory = cfg.remote.getDirectoryRoot(cfg.endpoint);
    boolean additionalDebug = cfg.remote.additionalDebug();
    List<AdaptrisMessage> result = new ArrayList<>();
    try {
      if (additionalDebug) {
        log.trace("Polling {}", pollDirectory);
      }
      String[] files = ftpClient.dir(pollDirectory, FsHelper.createFilter(cfg.filterExpression, fileFilterImp()));
      if (additionalDebug) {
        log.trace("There are potentially [{}] messages to aggregate", files.length);
      }
      for (int i = 0; i < files.length; i++) {
        String fullPath = pollDirectory + FORWARD_SLASH + FtpHelper.getFilename(files[i], cfg.remote.windowsWorkaround());
        result.add(fetch(ftpClient, fullPath, additionalDebug, factory));
      }
    }
    finally {
      cfg.remote.disconnect(ftpClient);
    }
    return result;
  }

  private List<AdaptrisMessage> single(ConfigWrapper cfg, AdaptrisMessageFactory factory) throws Exception {
    FileTransferClient ftpClient = cfg.remote.connect(cfg.endpoint);
    List<AdaptrisMessage> result = new ArrayList<>();
    try {
      String fullPath = cfg.remote.getDirectoryRoot(cfg.endpoint);
      result = Arrays.asList(fetch(ftpClient, fullPath, cfg.remote.additionalDebug(), factory));
    }
    finally {
      cfg.remote.disconnect(ftpClient);
    }
    return result;
  }

  private AdaptrisMessage fetch(FileTransferClient client, String fullPath, boolean debug, AdaptrisMessageFactory factory)
      throws Exception {
    String filename = FtpHelper.getFilename(fullPath);
    if (debug) {
      log.trace("Fetching [{}]", fullPath);
    }
    EncoderWrapper encoderSupport = new EncoderWrapper(factory.newMessage(), getEncoder());
    try (EncoderWrapper encoder = encoderSupport) {
      client.get(encoder, fullPath);
    }
    AdaptrisMessage msg = encoderSupport.build();
    msg.addObjectHeader(OBJ_METADATA_KEY_FILENAME, fullPath);
    msg.addMetadata(CoreConstants.ORIGINAL_NAME_KEY, filename);
    msg.addMetadata(CoreConstants.FS_FILE_SIZE, "" + msg.getSize());
    return msg;
  }

  String fileFilterImp() {
    return getFileFilterImp() != null ? getFileFilterImp() : DEFAULT_FILE_FILTER_IMP;
  }

  /**
   * @return the fileFilterImp
   */
  public String getFileFilterImp() {
    return fileFilterImp;
  }

  /**
   * Set the file filter implementation that will be used.
   * 
   * @param classname the fileFilterImp to set, defaults to
   */
  public void setFileFilterImp(String classname) {
    this.fileFilterImp = classname;
  }

  /**
   * @return the encoder
   */
  public AdaptrisMessageEncoder getEncoder() {
    return encoder;
  }

  /**
   * Set the encoder to use when reading files.
   * 
   * @param ame the encoder to set
   */
  public void setEncoder(AdaptrisMessageEncoder ame) {
    this.encoder = ame;
  }

  /**
   * @return the deleteAggregatedFiles
   */
  public Boolean getDeleteAggregatedFiles() {
    return deleteAggregatedFiles;
  }

  /**
   * Set whether to delete aggregated files.
   *
   * @param b defaults to true.
   */
  public void setDeleteAggregatedFiles(Boolean b) {
    this.deleteAggregatedFiles = b;
  }

  boolean deleteAggregatedFiles() {
    return BooleanUtils.toBooleanDefaultIfNull(getDeleteAggregatedFiles(), true);
  }

  private class ConfigWrapper {
    FileTransferConnection remote;
    String endpoint;
    String filterExpression;

    private ConfigWrapper(FileTransferConnection c, String e, String f) {
      remote = c;
      endpoint = e;
      filterExpression = f;
    }
  }
}
