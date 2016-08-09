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

package com.adaptris.core.services.duplicate;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;

/**
 * <p>
 * Abstract super-class of the two <code>Service</code>s which handle duplicate message checking.
 * </p>
 * <p>
 * In the adapter configuration file this class is aliased as <b>duplicate-metadata-value-service</b> which is the preferred
 * alternative to the fully qualified classname when building your configuration.
 * </p>
 * 
 * 
 */
public abstract class DuplicateMetadataValueService extends ServiceImp {

  @NotBlank
  private String metadataKey;
  @NotBlank
  private String storeFileUrl;

  // not marshalled
  protected transient List<Object> previousValuesStore;
  protected transient File store;

  @Override
  protected void initService() throws CoreException {
    if (getMetadataKey() == null) {
      throw new CoreException("metadataKeyToCheck must be set");
    }

    createStoreFile();
    loadPreviouslyReceivedValues();

    if (previousValuesStore == null) {
      previousValuesStore = new ArrayList<Object>();
    }
  }

  @Override
  protected void closeService() {}


  private void createStoreFile() throws CoreException {
    if (getStoreFileUrl() == null) {
      throw new CoreException("store file URL is null");
    }

    URL url = null;

    try {
      url = new URL(getStoreFileUrl());
    }
    catch (MalformedURLException e) {
      throw new CoreException(e);
    }

    store = new File(url.getFile());
  }

  protected void loadPreviouslyReceivedValues() throws ServiceException {
    if (store != null) {
      try {
        if (store.exists()) {
          ObjectInputStream o = new ObjectInputStream(
              new FileInputStream(store));

          previousValuesStore = (ArrayList<Object>) o.readObject();
          o.close();
        }
      }
      catch (Exception e) {
        throw new ServiceException(e);
      }
    }
  }

  int storeSize() {
    return previousValuesStore.size();
  }

  void deleteStore() {
    store.delete();
  }

  // properties...

  /**
   * <p>
   * Returns the metadata key whose value should be checked.
   * </p>
   *
   * @return metadataKey the metadata key whose value should be checked
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * <p>
   * Sets the metadata key whose value should be checked. May not be null.
   * </p>
   *
   * @param s the metadata key whose value should be checked
   */
  public void setMetadataKey(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    metadataKey = s;
  }

  /**
   * <p>
   * Returns the persistent store for previously received values in the form of
   * a file URL. E.g. <code>file:////Users/adaptris/store.dat/</code>.
   * </p>
   *
   * @return the persistent store for previously received values in the form of
   *         a file URL
   */
  public String getStoreFileUrl() {
    return storeFileUrl;
  }

  /**
   * <p>
   * Sets the persistent store for previously received values in the form of a
   * file URL. E.g. <code>file:////Users/adaptris/store.dat</code>. May not
   * be null or empty.
   * </p>
   *
   * @param s the persistent store for previously received values in the form of
   *          a file URL
   */
  public void setStoreFileUrl(String s) {
    if (s == null || "".equals(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    storeFileUrl = s;
  }


  @Override
  public void prepare() throws CoreException {
  }


}
