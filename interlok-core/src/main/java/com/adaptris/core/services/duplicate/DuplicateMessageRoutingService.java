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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import javax.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Class which checks the value of a configured metadata key against a list of previously received values. If the value appears in
 * the list, the value of <code>getDuplicateDestination</code> is stored against the value of <code>getDestinationKey</code>, if it
 * does not the value of <code>getUniqueDestination</code> is used.
 * </p>
 * <p>
 * This class should really just set an <code>isDuplicate</code> metadata key to true and leave the destination selection to
 * <code>MetadataBranchingService</code>.
 * </p>
 * 
 * @config duplicate-message-routing-service
 * 
 */
@XStreamAlias("duplicate-message-routing-service")
@AdapterComponent
@ComponentProfile(summary = "Set Metadata based on whether the message is considered a duplicate or not", tag = "service,duplicate")
@DisplayOrder(order = {"keyToCheck", "destinationKey", "uniqueDestination", "duplicateDestination", "configLocation"})
public class DuplicateMessageRoutingService extends ServiceImp {

  private static final Logger slf4jLogger = LoggerFactory.getLogger(DuplicateMessageRoutingService.class);
  /**
   * <p>
   * Metadata key against which the value to check for uniqueness is stored.
   * </p>
   */
  public static final String DEFAULT_CHECK_KEY = "DuplicateCheck";

  @InputFieldDefault(value = "100")
  private int historySize;
  @NotBlank
  @AutoPopulated
  private String keyToCheck = DEFAULT_CHECK_KEY;
  @NotBlank
  private String destinationKey = null;
  @NotBlank
  private String uniqueDestination = null;
  @NotBlank
  private String duplicateDestination = null;
  @NotBlank
  private String configLocation;

  // not marshalled
  private transient File file = null;
  private transient LinkedList<String> comparators = new LinkedList<String>();

  public DuplicateMessageRoutingService() {
    setHistorySize(100);
  }

  /**
   * <p>
   * Sets the number of elements to keep in the previously received IDs list.
   * Default is 100
   * </p>
   *
   * @param i the number of message ids to retain
   */
  public void setHistorySize(int i) {
    historySize = i;
  }

  /**
   * <p>
   * Gets the number of elements to keep in the previously received IDs list.
   * Default is 100
   * </p>
   *
   * @return the number of message ids to retain
   */
  public int getHistorySize() {
    return historySize;
  }

  /**
   * <p>
   * Sets the key for the MetadataElement to check for uniqueness. Default is
   * "DuplicateCheck".
   * </p>
   *
   * @param key the Key to check
   */
  public void setKeyToCheck(String key) {
    keyToCheck = key;
  }

  /**
   * <p>
   * Gets the key for the MetadataElement to check for uniqueness.
   * </p>
   *
   * @return the Key to check
   */
  public String getKeyToCheck() {
    return keyToCheck;
  }

  /**
   * <p>
   * Sets the location for the file that will contain the persisted list of
   * received ids.
   * </p>
   *
   * @param s a URI which must be in the form "file:///filepath"
   */
  public void setConfigLocation(String s) {
    configLocation = s;
  }

  /**
   * <p>
   * Gets the location for the file that will contain the persisted list of
   * received ids.
   * </p>
   *
   * @return the location of the file
   */
  public String getConfigLocation() {
    return configLocation;
  }

  /**
   * <p>
   * Sets the Key for the output MetadataElement which will contain the output
   * destination.
   * </p>
   *
   * @param key the Key to set against
   */
  public void setDestinationKey(String key) {
    destinationKey = key;
  }

  /**
   * <p>
   * Gets the Key for the output MetadataElement which will contain the output
   * destination.
   * </p>
   *
   * @return the Key to set against
   */
  public String getDestinationKey() {
    return destinationKey;
  }

  /**
   * <p>
   * Sets the output destination String which will be stored as the value of the
   * MetadataElement if the id is unique.
   * </p>
   *
   * @param destination the String to set on the output MetadataElement
   */
  public void setUniqueDestination(String destination) {
    uniqueDestination = destination;
  }

  /**
   * <p>
   * Gets the output destination String which will be stored as the value of the
   * MetadataElement if the id is unique.
   * </p>
   *
   * @return the String to set on the output MetadataElement
   */
  public String getUniqueDestination() {
    return uniqueDestination;
  }

  /**
   * <p>
   * Sets the output destination String which will be stored as the value of the
   * MetadataElement if the id is not unique.
   * </p>
   *
   * @param destination the String to set on the output MetadataElement
   */
  public void setDuplicateDestination(String destination) {
    duplicateDestination = destination;
  }

  /**
   * <p>
   * Sets the output destination String which will be stored as the value of the
   * MetadataElement if the id is not unique.
   * </p>
   *
   * @return the String to set on the output MetadataElement
   */
  public String getDuplicateDestination() {
    return duplicateDestination;
  }

  private boolean isDuplicate(String id) {
    if (!comparators.contains(id)) {
      comparators.addFirst(id);

      while (comparators.size() > historySize) {
        comparators.removeLast();
      }

      return false;
    }

    return true;
  }

  private void store() throws IOException {
    try (FileOutputStream out = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(out)) {
      oos.writeObject(comparators);
    }
  }

  private void load() throws Exception {
    if (file.exists()) {
      try (FileInputStream in = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(in)) {
        comparators = (LinkedList) ois.readObject();
      }
    }
  }

  /**
   * <p>
   * Method which checks to see if the message is unique (based on a metadata
   * element) and sets a new metadata element with a value which is dependant
   * upon the uniqueness or otherwise of the received element.
   * </p>
   *
   * @param msg the input AdaptrisMessage
   * @throws ServiceException wrapping any underlying Exception
   */
  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {

    String id = msg.getMetadataValue(keyToCheck);

    if (isDuplicate(id)) {
      msg.addMetadata(destinationKey, duplicateDestination);
    }
    else {
      msg.addMetadata(destinationKey, uniqueDestination);
    }
  }

  @Override
  protected void initService() throws CoreException {
    tryAndWrap(() -> {
      file = FsHelper.toFile(getConfigLocation());
      load();
    });
  }

  @Override
  protected void closeService() {
    tryAndLog("Failed to shutdown component cleanly, logging exception for informational purposes only", () -> store());
  }


  @Override
  public void start() throws CoreException {
    tryAndWrap(() -> load());
  }

  @Override
  public void stop() {
    tryAndLog("Failed to stop component cleanly, logging exception for informational purposes only", () -> store());
  }

  @Override
  public void prepare() throws CoreException {}


  protected static void tryAndLog(String logMsg, Operator l) {
    try {
      l.doOperation();
    } catch (Exception e) {
      slf4jLogger.trace(logMsg, e);
    }
  }

  protected static void tryAndWrap(Operator l) throws CoreException {
    try {
      l.doOperation();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @FunctionalInterface
  protected interface Operator {
    void doOperation() throws Exception;
  }
}
