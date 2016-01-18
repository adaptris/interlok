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

package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.BranchingServiceImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Branching <code>Service</code> implementation which checks the value stored against a configured metadata key against a list of
 * previously received values.
 * <p>
 * The service obeys the following rules when checking the metadata key
 * <ul>
 * <li>If the looked-up value is null or empty, a {@link ServiceException} is thrown.</li>
 * <li>If the value is set and has previously been received, the configured <code>nextServiceIdIfDuplicate</code> is set on the
 * message.</li>
 * <li>If the looked-up value is not contained in the store of previous values <code>nextServiceIdIfUnique</code> is set and the
 * value is added to this store for future checking.</li>
 * </p>
 * <p>
 * The store of previous values has a configurable maximum size. After a new value is added, if the store exceeds the maximum size
 * the oldest value is removed. The store is then persisted to the configured store file.
 * </p>
 * 
 * @config check-unique-metadata-value-service
 * 
 * 
 */
@XStreamAlias("check-unique-metadata-value-service")
@AdapterComponent
@ComponentProfile(
    summary = "Perform a branch by checking a metadata key and comparing it against a list of previously received values",
    tag = "service,branching")
public class CheckUniqueMetadataValueService extends BranchingServiceImp {

  /**
   * <p>
   * Default next Service ID to set if the message metadata value does not
   * appear in the store of previously received values.
   * </p>
   */
  public static final String DEFAULT_SERVICE_ID_UNIQUE = "001";

  /**
   * <p>
   * Default next Service ID to set if the message metadata value <em>does</em>
   * appear in the store of previously received values.
   * </p>
   */
  public static final String DEFAULT_SERVICE_ID_DUPLICATE = "002";

  @NotBlank
  private String metadataKeyToCheck;
  @NotBlank
  private String storeFileUrl;
  @NotBlank
  private String nextServiceIdIfDuplicate;
  @NotBlank
  private String nextServiceIdIfUnique;
  private int numberOfPreviousValuesToStore;

  // not marshalled
  private transient List<Object> previousValuesStore;
  private transient File store;

  /**
   * <p>
   * Creates a new instance. Default history size is 1000.
   * </p>
   */
  public CheckUniqueMetadataValueService() {
    this.setNumberOfPreviousValuesToStore(1000);

    // this.setNextServiceIdIfUnique(DEFAULT_SERVICE_ID_UNIQUE);
    // this.setNextServiceIdIfDuplicate(DEFAULT_SERVICE_ID_DUPLICATE);
  }

  @Override
  protected void initService() throws CoreException {
    if (this.getMetadataKeyToCheck() == null) {
      throw new CoreException("metadataKeyToCheck must be set");
    }

    this.createStoreFile();
    this.loadPreviouslyReceivedValues();

    if (previousValuesStore == null) {
      previousValuesStore = new ArrayList<Object>();
    }
  }

  @Override
  protected void closeService() {

  }


  private void createStoreFile() throws CoreException {
    if (this.getStoreFileUrl() == null) {
      throw new CoreException("store file URL is null");
    }

    URL url = null;

    try {
      url = new URL(this.getStoreFileUrl());
    }
    catch (MalformedURLException e) {
      throw new CoreException(e);
    }

    this.store = new File(url.getFile());
  }

  /**
   * @see com.adaptris.core.Service
   *      #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String value = msg.getMetadataValue(this.getMetadataKeyToCheck());

    if (value == null || "".equals(value)) {
      throw new ServiceException("required metadata [" + this.getMetadataKeyToCheck() + "] missing");
    }

    if (previousValuesStore.contains(value)) {
      this.handleDuplicate(msg, value);
    }
    else {
      try {
        this.handleNewValue(msg, value);
      }
      catch (Exception e) {
        throw new ServiceException(e);
      }
    }
  }

  private void handleDuplicate(AdaptrisMessage msg, String value) throws ServiceException {

    String errorMessage = this.createErrorMessage(value);
    log.warn(errorMessage);

    msg.setNextServiceId(this.getNextServiceIdIfDuplicate());
  }

  private String createErrorMessage(String value) {
    StringBuffer result = new StringBuffer();
    result.append("value [");
    result.append(value);
    result.append("] stored against key [");
    result.append(this.getMetadataKeyToCheck());
    result.append("] exists in list of previously stored values");

    return result.toString();
  }

  private void handleNewValue(AdaptrisMessage msg, String value) throws Exception {

    msg.setNextServiceId(this.getNextServiceIdIfUnique());

    previousValuesStore.add(value);

    while (previousValuesStore.size() > this.getNumberOfPreviousValuesToStore()) {

      previousValuesStore.remove(0);
    }

    this.storePreviouslyReceivedValues();
  }

  private void storePreviouslyReceivedValues() {
    if (store != null) {
      try {
        ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(store));

        o.writeObject(previousValuesStore);
        o.flush();
        o.close();
      }
      catch (Exception e) {
        log.error("exception storing previously received values", e);
        log.error(previousValuesStore.toString());
      }
    }
  }

  private void loadPreviouslyReceivedValues() throws CoreException {
    if (store != null) {
      try {
        if (store.exists()) {
          ObjectInputStream o = new ObjectInputStream(new FileInputStream(store));

          previousValuesStore = (ArrayList<Object>) o.readObject();
          o.close();
        }
      }
      catch (Exception e) {
        throw new CoreException(e);
      }
    }
  }

  int storeSize() {
    return previousValuesStore.size();
  }

  // properties...

  /**
   * <p>
   * Returns the metadata key whose value should be checked.
   * </p>
   *
   * @return metadataKey the metadata key whose value should be checked
   */
  public String getMetadataKeyToCheck() {
    return this.metadataKeyToCheck;
  }

  /**
   * <p>
   * Sets the metadata key whose value should be checked. May not be null.
   * </p>
   *
   * @param s the metadata key whose value should be checked
   */
  public void setMetadataKeyToCheck(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.metadataKeyToCheck = s;
  }

  /**
   * <p>
   * Returns the number of previous values to keep.
   * </p>
   *
   * @return the number of previous values to keep
   */
  public int getNumberOfPreviousValuesToStore() {
    return this.numberOfPreviousValuesToStore;
  }

  /**
   * <p>
   * Sets the number of previous values to keep. Must be greater than 0.
   * </p>
   *
   * @param i the number of previous values to keep
   */
  public void setNumberOfPreviousValuesToStore(int i) {
    if (i < 1) {
      throw new IllegalArgumentException("history size is 0 or negative");
    }
    this.numberOfPreviousValuesToStore = i;
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
    return this.storeFileUrl;
  }

  /**
   * <p>
   * Sets the persistent store for previously received values in the form of a
   * file URL. E.g. <code>file:////Users/adaptris/store.dat</code>. May not be
   * null or empty.
   * </p>
   *
   * @param s the persistent store for previously received values in the form of
   *          a file URL
   */
  public void setStoreFileUrl(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.storeFileUrl = s;
  }

  /**
   * <p>
   * Returns the ID of the next <code>Service</code> to apply if the metadata
   * exists if the store of previous values.
   * </p>
   *
   * @return the ID of the next <code>Service</code> to apply if the metadata
   *         exists if the store of previous values
   */
  public String getNextServiceIdIfDuplicate() {
    return this.nextServiceIdIfDuplicate;
  }

  /**
   * <p>
   * Sets the ID of the next <code>Service</code> to apply if the metadata
   * exists if the store of previous values. May not be null or empty.
   * </p>
   *
   * @param s the ID of the next <code>Service</code> to apply if the metadata
   *          exists if the store of previous values
   */
  public void setNextServiceIdIfDuplicate(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.nextServiceIdIfDuplicate = s;
  }

  /**
   * <p>
   * Returns the ID of the next <code>Service</code> to apply if the metadata
   * does not exist if the store of previous values.
   * </p>
   *
   * @return the ID of the next <code>Service</code> to apply if the metadata
   *         does not exist if the store of previous values
   */
  public String getNextServiceIdIfUnique() {
    return this.nextServiceIdIfUnique;
  }

  /**
   * <p>
   * Sets the ID of the next <code>Service</code> to apply if the metadata does
   * not exist if the store of previous values. May not be null or empty.
   * </p>
   *
   * @param s the ID of the next <code>Service</code> to apply if the metadata
   *          does not exist if the store of previous values
   */
  public void setNextServiceIdIfUnique(String s) {
    if (isEmpty(s)) {
      throw new IllegalArgumentException("null or empty param");
    }
    this.nextServiceIdIfUnique = s;
  }

  @Override
  public void prepare() throws CoreException {}

}
