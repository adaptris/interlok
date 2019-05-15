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

import static org.apache.commons.lang.StringUtils.isEmpty;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageEncoder;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.services.aggregator.AggregatingConsumerImpl;
import com.adaptris.core.services.aggregator.ConsumeDestinationGenerator;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link com.adaptris.core.services.aggregator.AggregatingConsumer} implementation that allows you to read a separate message from
 * the filesystem that is correlated in some way to the current message.
 * <p>
 * You need to configure a {@link ConsumeDestinationGenerator} implementation; which is subsequently used to generate the fully
 * qualified URL to the destination message e.g. <code>file:///C:/path/to/correlated/message</code>. If the file does not exist, is
 * inaccessible or not a file, then an exception is thrown.
 * </p>
 * <p>
 * If a filter-expression of the generated {@link ConsumeDestination} is available, then this works as a true aggregator; it will
 * trigger the use of a FileFilter, and ultimately cause multiple files to be read and passed to the configured message aggregator.
 * Note that no decision is made about the resulting size of the message, all messages that match the filter-expression will be
 * aggregated; if there a 2000 files sitting in a directory that match the filter-expression, then that is how many will be picked
 * up.
 * </p>
 * 
 * @config aggregating-fs-consumer
 * 
 */
@XStreamAlias("aggregating-fs-consumer")
@DisplayOrder(order = {"destination", "messageAggregator", "filterFilterImp", "wipSuffix", "encoder"})
public class AggregatingFsConsumer extends AggregatingConsumerImpl<AggregatingFsConsumeService> {
  private static final String DEFAULT_FILE_FILTER_IMP = "org.apache.commons.io.filefilter.RegexFileFilter";
  private static final String DEFAULT_WIP_SUFFIX = "_wip";
  private static final String OBJ_METADATA_KEY_FILE = AggregatingFsConsumer.class.getCanonicalName() + ".file";
  private static final String OBJ_METADATA_KEY_FILENAME = AggregatingFsConsumer.class.getCanonicalName() + ".filename";

  private String fileFilterImp;
  @AdvancedConfig
  private Boolean deleteAggregatedFiles;
  @AdvancedConfig
  private String wipSuffix;
  @Valid
  @AdvancedConfig
  private AdaptrisMessageEncoder encoder;

  private transient FsWorker fsWorker = new NioWorker();

  public AggregatingFsConsumer() {

  }

  public AggregatingFsConsumer(ConsumeDestinationGenerator d) {
    this();
    setDestination(d);
  }

  @Override
  public void aggregateMessages(AdaptrisMessage msg, AggregatingFsConsumeService service) throws ServiceException {
    ConsumeDestination dest = getDestination().generate(msg);
    List<AdaptrisMessage> result = new ArrayList<>();
    try {
      result = isEmpty(dest.getFilterExpression()) ? readSingleFile(dest, msg.getFactory()) : readMultipleFiles(dest,
          msg.getFactory());
      getMessageAggregator().joinMessage(msg, result);
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
    if(isDeleteAggregatedFiles()) {
      // At this point, the messages have been aggregated so we can delete all the "files" associated with the message.
      deleteAggregatedFiles(result);
    } else {
      renameAggregatedFiles(result);
    }
  }

  private void deleteAggregatedFiles(List<AdaptrisMessage> msgs) {
    for (AdaptrisMessage m : msgs) {
      Map<?,?> objectMetadata = m.getObjectHeaders();
      if (objectMetadata.containsKey(OBJ_METADATA_KEY_FILE)) {
        log.trace("Deleting aggregated file : " + objectMetadata.get(OBJ_METADATA_KEY_FILENAME));
        ((File) objectMetadata.get(OBJ_METADATA_KEY_FILE)).delete();
      }
    }
  }
  private void renameAggregatedFiles(List<AdaptrisMessage> msgs) {
    for (AdaptrisMessage m : msgs) {
      Map<?,?> objectMetadata = m.getObjectHeaders();
      if (objectMetadata.containsKey(OBJ_METADATA_KEY_FILE)) {
        File f = (File) objectMetadata.get(OBJ_METADATA_KEY_FILE);
        File parent = f.getParentFile();
        String name = f.getName().replaceAll(wipSuffix().replaceAll("\\.", "\\\\."), "");
        log.trace("Will Rename " + f.getName() + " back to " + name);
        File newFile = new File(parent, name);
        f.renameTo(newFile);
      }
    }
  }

  private List<AdaptrisMessage> readMultipleFiles(ConsumeDestination dest, AdaptrisMessageFactory factory) throws ServiceException {
    ArrayList<AdaptrisMessage> result = new ArrayList<>();
    try {
      String baseUrl = dest.getDestination();
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      File directory = FsHelper.createFileReference(url);
      FileFilter filter = createFileFilter(dest.getFilterExpression());
      File[] files = directory.listFiles(filter);
      for (File f : files) {
        result.add(read(f, factory));
      }
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
    return result;
  }

  private List<AdaptrisMessage> readSingleFile(ConsumeDestination dest, AdaptrisMessageFactory factory) throws ServiceException {
    AdaptrisMessage newMsg = null;
    try {
      String baseUrl = dest.getDestination();
      URL url = FsHelper.createUrlFromString(baseUrl, true);
      File fileToRead = FsHelper.createFileReference(url);
      newMsg = read(fileToRead, factory);
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
    return Arrays.asList(new AdaptrisMessage[]
    {
      newMsg
    });
  }

  private AdaptrisMessage read(File src, AdaptrisMessageFactory factory) throws ServiceException {
    AdaptrisMessage msg = null;    
    try {
      log.trace("Reading " + src.getCanonicalPath());
      File wipFile = FsHelper.renameFile(src, wipSuffix(), fsWorker);
      msg = decode(fsWorker.get(wipFile), factory);
      msg.addObjectHeader(OBJ_METADATA_KEY_FILE, wipFile);
      msg.addObjectHeader(OBJ_METADATA_KEY_FILENAME, src.getCanonicalPath());
    }
    catch (Exception e) {
      rethrowServiceException(e);
    }
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

  private FileFilter createFileFilter(String filterExpression) throws Exception {
    Class[] paramTypes =
    {
      filterExpression.getClass()
    };
    Object[] args =
    {
      filterExpression
    };

    Class c = Class.forName(fileFilterImp());
    Constructor cnst = c.getDeclaredConstructor(paramTypes);
    return (FileFilter) cnst.newInstance(args);
  }

  private AdaptrisMessage decode(byte[] bytes, AdaptrisMessageFactory factory) throws CoreException {
    if (getEncoder() != null) {
      getEncoder().registerMessageFactory(factory);
      ByteArrayInputStream in = new ByteArrayInputStream(bytes);
      return encoder.readMessage(in);
    }
    return factory.newMessage(bytes);
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
   * @return the wipSuffix
   */
  public String getWipSuffix() {
    return wipSuffix;
  }

  /**
   * Sets the work-in-progress suffix to use.
   * <p>
   * This suffix is added to the original file name while the file is being processed.
   * </p>
   * 
   * @param suffix the wipSuffix to set, if not explicitly configured defaults to '_wip'
   */
  public void setWipSuffix(String suffix) {
    this.wipSuffix = suffix;
  }

  String wipSuffix() {
    return !isEmpty(getWipSuffix()) ? getWipSuffix() : DEFAULT_WIP_SUFFIX;
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
   * @param deleteAggregatedFiles defaults to true.
   */
  public void setDeleteAggregatedFiles(Boolean deleteAggregatedFiles) {
    this.deleteAggregatedFiles = deleteAggregatedFiles;
  }

  Boolean isDeleteAggregatedFiles(){
    return getDeleteAggregatedFiles() == null ? true : getDeleteAggregatedFiles();
  }


}
