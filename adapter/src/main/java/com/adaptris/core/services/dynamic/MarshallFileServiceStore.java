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

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;

/**
 * Abstract base implementation of a file based service store that uses your choice of 
 * marshalling technology.
 *
 * @author lchan
 * @author $Author: lchan $
 */
public abstract class MarshallFileServiceStore extends MarshallServiceStore {
  @NotNull
  @InputFieldDefault(value = "")
  private String fileNameSuffix;
  @NotNull
  @InputFieldDefault(value = "")
  @AdvancedConfig
  private String fileNamePrefix;
  @AdvancedConfig
  private String defaultFileName;

  /**
   * <p>
   * Creates a new instance. Default is no <code>fileNameSuffix</code> and no
   * <code>fileNamePrefix</code>.
   * </p>
   * 
   *
   * @throws CoreException wrapping any Exceptions which occur
   */
  public MarshallFileServiceStore() throws CoreException {
    super();
    setFileNamePrefix("");
    setFileNameSuffix("");
  }

  /**
   * <p>
   * Returns the (optional) suffix to append to the logical <code>Service</code>
   * name to create the name of the file to read.
   * </p>
   *
   * @return the (optional) suffix to append to the logical <code>Service</code>
   *         name to create the name of the file to read
   */
  public String getFileNameSuffix() {
    return fileNameSuffix;
  }

  /**
   * <p>
   * Sets the (optional) suffix to append to the logical <code>Service</code>
   * name to create the name of the file to read. May not be null.
   * </p>
   *
   * @param s the (optional) suffix to append to the logical
   *          <code>Service</code> name to create the name of the file to read
   */
  public void setFileNameSuffix(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    fileNameSuffix = s;
  }

  /**
   * <p>
   * Returns the (optional) prefix to prepend to the logical
   * <code>Service</code> name to create the name of the file to read. May not
   * be null.
   * </p>
   *
   * @return the (optional) prefix to prepend to the logical
   *         <code>Service</code> name to create the name of the file to read
   */
  public String getFileNamePrefix() {
    return fileNamePrefix;
  }

  /**
   * <p>
   * Sets the (optional) prefix to prepend to the logical <code>Service</code>
   * name to create the name of the file to read. May not be null.
   * </p>
   *
   * @param s the (optional) prefix to prepend to the logical
   *          <code>Service</code> name to create the name of the file to read
   */
  public void setFileNamePrefix(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null param");
    }
    fileNamePrefix = s;
  }

  /**
   * @return the defaultFileName
   */
  public String getDefaultFileName() {
    return defaultFileName;
  }

  /**
   * Set the default filename that will be used if the file containing the
   * service list could not be found.
   *
   * <p>
   * The fileNamePrefix and filenameSuffix will be added to the file before
   * attempting to load it
   * </p>
   *
   * @param s the defaultFileName to set
   * @see ServiceStore#obtain(String)
   */
  public void setDefaultFileName(String s) {
    defaultFileName = s;
  }

  /** @see com.adaptris.core.services.dynamic.ServiceStore#obtain(String) */
  @Override
  public Service obtain(String s) throws CoreException {
    Service result = null;
    result = unmarshal(s);
    if (result == null && getDefaultFileName() != null) {
      result = unmarshal(getDefaultFileName());
    }
    return result;
  }

  /**
   * Unmarshal the Service from the store.
   *
   * @param s the name under which the service is stored.
   * @return the unmarshalled Service implementation.
   * @throws CoreException wrapping any underlying exceptions.
   */
  protected abstract Service unmarshal(String s) throws CoreException;
}
