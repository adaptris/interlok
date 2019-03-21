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

package com.adaptris.core.services.dynamic;

import static com.adaptris.fs.FsWorker.checkReadable;
import static com.adaptris.fs.FsWorker.isDirectory;
import static com.adaptris.fs.FsWorker.isFile;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import java.io.File;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ServiceStore</code> which uses the local file system to store marshalled
 * <code>Service</code>s.
 * </p>
 * 
 * @config local-marshall-service-store
 * @deprecated since 3.8.4 use {@link DynamicServiceExecutor} with a URL based
 *             {@link ServiceExtractor} instead.
 */
@Deprecated
@XStreamAlias("local-marshall-service-store")
@Removal(version = "3.11.0")
public class LocalMarshallServiceStore extends MarshallFileServiceStore {

  @NotBlank
  @AutoPopulated
  private String baseDirUrl;
  private transient File baseDir;

  /**
   * <p>
   * Creates a new instance. Default is no <code>fileNameSuffix</code> and no <code>fileNamePrefix</code>. Default imp. class is
   * <code>com.adaptris.core.ServiceList</code>.
   * </p>
   * <p>
   * No default <code>baseDirUrl</code> is set. If it is not explicitly set, this class will look for marshalled classes in the
   * working directory. It is strongly recommended that <code>baseDirUrl</code> is explicitly set.
   * </p>
   * 
   * @throws CoreException wrapping any Exceptions which occur
   */
  public LocalMarshallServiceStore() throws CoreException {
    super();
    setBaseDirUrl("file:///" + new File(".").getAbsolutePath().replaceAll("\\\\", "/"));
  }

  public LocalMarshallServiceStore(String url, String prefix, String suffix, String defaultFilename) throws CoreException {
    this();
    setBaseDirUrl(url);
    setFileNamePrefix(prefix);
    setFileNameSuffix(suffix);
    setDefaultFileName(defaultFilename);
  }

  /** @see com.adaptris.core.services.dynamic.ServiceStore#validate() */
  @Override
  public void validate() throws CoreException {
    try {
      Args.notBlank(getBaseDirUrl(), "baseDirUrl");
      baseDir = isDirectory(checkReadable(FsHelper.createFileReference(FsHelper.createUrlFromString(baseDirUrl, true))));
      log.trace("Base directory validated [{}]", baseDir);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected Service unmarshal(String s) throws CoreException {
    Service result = null;

    String filename = defaultIfEmpty(getFileNamePrefix(), "") + s + defaultIfEmpty(getFileNameSuffix(), "");
    try {
      if (baseDir == null) {
        validate();
      }
      result = (Service) currentMarshaller().unmarshal(isFile(checkReadable(new File(baseDir, filename))));
      log.trace("service file name [{}] found in store", filename);
    }
    catch (Exception e) {
      log.debug("service file name [{}] not found in store", filename);
      result = null;
    }
    return result;
  }

  /**
   * <p>
   * Returns the base directory of the store in the form of a file URL.
   * </p>
   * 
   * @return the base directory of the store in the form of a file URL
   */
  public String getBaseDirUrl() {
    return baseDirUrl;
  }

  /**
   * <p>
   * Sets the base directory of the store in the form of a file URL. E.g. <code>file:////Users/adaptris/services/</code>. May not be
   * null or empty.
   * </p>
   * 
   * @param s the base directory of the store in the form of a file URL
   */
  public void setBaseDirUrl(String s) {
    baseDirUrl = Args.notBlank(s,  "baseDirUrl");
  }

}
