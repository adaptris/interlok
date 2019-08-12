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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import org.hibernate.validator.constraints.NotBlank;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Implementation of <code>ServiceStore</code> which uses a remote URL to store marshalled
 * <code>Service</code>s.
 * </p>
 * 
 * @config remote-marshall-service-store
 * @deprecated since 3.8.4 use {@link DynamicServiceExecutor} with a URL based
 *             {@link ServiceExtractor} instead.
 */
@Deprecated
@XStreamAlias("remote-marshall-service-store")
@DisplayOrder(order = {"baseUrl"})
@Removal(version = "3.11.0")
public class RemoteMarshallServiceStore extends MarshallFileServiceStore {

  @NotBlank
  private String baseUrl;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   *
   * @throws CoreException wrapping any Exceptions which occur
   */
  public RemoteMarshallServiceStore() throws CoreException {
    super();
  }

  public RemoteMarshallServiceStore(String url, String prefix, String suffix, String defaultFilename) throws CoreException {
    this();
    setBaseUrl(url);
    setFileNamePrefix(prefix);
    setFileNameSuffix(suffix);
    setDefaultFileName(defaultFilename);
  }


  @Override
  public void validate() throws CoreException {
    try {
      Args.notBlank(getBaseUrl(), "baseUrl");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected Service unmarshal(String s) throws CoreException {
    Service result = null;

    String remoteFile = getBaseUrl() + "/" + defaultIfEmpty(getFileNamePrefix(), "") + s + defaultIfEmpty(getFileNameSuffix(), "");
    try {
      URL url = new URL(remoteFile);
      log.debug("Retrieving [{}]", remoteFile);
      URLConnection c = url.openConnection();
      try (Reader reader = new InputStreamReader(c.getInputStream())) {
        result = (Service) currentMarshaller().unmarshal(reader);
      }
    }
    catch (IOException e) {
      if (e instanceof FileNotFoundException) {
        log.debug("service file name [{}] not found in store", remoteFile);
        result = null;
      }
      else {
        throw new CoreException(e);
      }
    }
    return result;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String s) {
    baseUrl = Args.notBlank(s, "baseUrl");
  }
}
