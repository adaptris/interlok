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

package com.adaptris.util.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * SimpleDataStore.
 * <p>
 * A simple concrete implementation of the datastore class.
 * <p>
 * The Datastore, takes a number of properties which determines its behaviour, these are:-
 * <p>
 * 
 * <pre>
 * {@code
 *  #The Fully qualified filename where data will be stored.
 *  simpledatastore.fileName
 *  #The Fully qualified filename where a semaphore lock file will be stored.
 *  simpledatastore.lockFile
 *  #The number of attempts made to get a lock before an error is thrown
 *  simpledatastore.maxLockAttempts
 * }
 * </pre>
 * </p>
 * 
 * @config simple-data-store
 * @see DataStore
 * @author $Author: lchan $
 */
@XStreamAlias("simple-data-store")
@DisplayOrder(order = {"dataFile", "lockFile", "maxAttempts"})
public class SimpleDataStore extends DataStore {

  /** The filename property */
  public static final String FILE_PROPERTY = "simpledatastore.file.name";
  /** The locfile property */
  public static final String LOCK_PROPERTY = "simpledatastore.lock.file";
  /** the max lock attempts property */
  public static final String MAXLOCK_PROPERTY = "simpledatastore.max.lock.attempts";

  /**
   * The default data filename.
   *
   */
  public static final String DEFAULT_FILENAME = "sds.dat";
  /**
   * The default lock file.
   *
   */
  public static final String DEFAULT_LOCK = "sds.dat.lock";

  @NotBlank
  @AutoPopulated
  private String dataFile = DEFAULT_FILENAME;
  @NotBlank
  @AutoPopulated
  private String lockFile = DEFAULT_LOCK;

  private int maxAttempts = 15;
  private transient boolean haveLock = false;

  private transient Logger logR = LoggerFactory.getLogger(this.getClass());

  /**
   * @see DataStore#DataStore()
   */
  public SimpleDataStore() {
    super();
    String tmpDir = System.getProperty("java.io.tmpdir");
    if (tmpDir != null) {
      if (!tmpDir.endsWith(File.separator)) {
        tmpDir = tmpDir + File.separator;
      }
      dataFile = tmpDir + DEFAULT_FILENAME;
      lockFile = tmpDir + DEFAULT_LOCK;
    }
    else {
      dataFile = DEFAULT_FILENAME;
      lockFile = DEFAULT_LOCK;
    }
  }

  /**
   * Constructor using a set of properties.
   *
   * @param p the properties.
   * @see #FILE_PROPERTY
   * @see #LOCK_PROPERTY
   * @see #MAXLOCK_PROPERTY
   * @throws DataStoreException if there is an error.
   */
  public SimpleDataStore(Properties p) throws DataStoreException {
    this();
    setConfiguration(p);
  }

  /**
   * @see DataStore#persist(String, String, Object)
   */
  @Override
  public void persist(String id, String type, Object obj) throws DataStoreException {
    if (id == null || type == null) {
      throw new DataStoreException("Neither the id or the type" + " can be NULL when persisting data.");
    }

    try {
      checkConfiguration();
      getLock();

      HashMap sharedData = readData();
      sharedData.put(id + type, obj);
      writeData(sharedData);
    }
    catch (Exception e) {
      throw new DataStoreException("The data could not be persisted.", e);
    }
    finally {
      removeLock();
    }
  }

  /**
   * @see DataStore#retrieve(String, String)
   */
  @Override
  public Object retrieve(String id, String type) throws DataStoreException {

    Object o = null;

    try {
      checkConfiguration();

      // this.getLock();
      HashMap sharedData = readData();
      o = sharedData.get(id + type);

      // We're only reading the file, so why re-write the data?
      // this.writeData(sharedData);
    }
    catch (Exception e) {
      throw new DataStoreException("Shared data could not be retrieved.", e);
    }
    finally {
      removeLock();
    }

    return o;
  }

  /**
   * @see DataStore#remove(String, String)
   */
  @Override
  public void remove(String id, String type) throws DataStoreException {
    try {
      checkConfiguration();
      getLock();

      HashMap sharedData = readData();
      sharedData.remove(id + type);
      writeData(sharedData);
    }
    catch (Exception e) {
      throw new DataStoreException("Shared data could not be removed.", e);
    }
    finally {
      removeLock();
    }
  }

  /**
   * @see DataStore#exists(String, String)
   */
  @Override
  public boolean exists(String id, String type) throws DataStoreException {

    boolean containsKey = false;

    try {
      checkConfiguration();

      HashMap data = readData();
      containsKey = data.containsKey(id + type);
    }
    catch (Exception e) {
      throw new DataStoreException("Datastore could not be examined.", e);
    }
    finally {
      removeLock();
    }

    return containsKey;
  }

  /**
   * Get a lock on the file
   *
   * @throws Exception if we could not get the lock.
   */
  public void getLock() throws Exception {

    if (haveLock) {
      return;
    }
    File lock = new File(getLockFileName());
    int attempts = 1;

    while (lock.exists() && attempts++ < maxAttempts) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException i) {
        ;
      }
    }
    if (!lock.exists() && lock.createNewFile()) {
      ;
    }
    else {
      throw new Exception("Couldn't get Lock");
    }
    haveLock = true;
    return;
  }

  /**
   * Remove the lock.
   */
  public void removeLock() {
    if (!haveLock) {
      return;
    }

    File lock = new File(getLockFileName());

    if (lock.exists()) {
      lock.delete();
    }

    haveLock = false;

    return;
  }

  private void checkConfiguration() throws Exception {
    if (dataFile == null || lockFile == null) {
      throw new Exception("Invalid Configuration");
    }
  }

  private void initFromProperties(Properties p) throws DataStoreException {
    setMaxAttempts(Integer.parseInt(p.getProperty(MAXLOCK_PROPERTY, "15")));
    setDataFileName(p.getProperty(FILE_PROPERTY));
    setLockFileName(p.getProperty(LOCK_PROPERTY));
  }

  // Read the data from the file.
  private HashMap readData() throws IOException, ClassNotFoundException {

    HashMap data = new HashMap();
    File file = new File(getDataFileName());
    FileInputStream is = null;
    ObjectInputStream oi = null;
    try {
      if (file.exists()) {

        // read the object in...
        is = new FileInputStream(file);
        oi = new ObjectInputStream(is);
        data = (HashMap) oi.readObject();
      }
      else {
        // logR.info("No existing data file found. New file: " + dataFileName);
        // file.createNewFile();
        ;
      }
    }
    finally {
      IOUtils.closeQuietly(oi);
      IOUtils.closeQuietly(is);
    }

    return data;
  }

  private void writeData(HashMap data) throws IOException {

    // write the object out...
    FileOutputStream out = new FileOutputStream(getDataFileName());
    ObjectOutputStream objOut = new ObjectOutputStream(out);
    objOut.writeObject(data);
    objOut.close();
    out.close();
  }

  /**
   * Get the data filename.
   *
   * @return the filename in url format.
   */
  public String getDataFileName() {
    return dataFile;
  }

  /**
   * Get the lock file name.
   *
   * @return the lock file in URL format.
   */
  public String getLockFileName() {
    return lockFile;
  }

  /**
   * Return the number of attempts to gain a lock
   *
   * @return the number of attempts.
   */
  public int getMaxAttempts() {
    return maxAttempts;
  }

  /**
   * The Data file stored url format.
   *
   * @param string the data file.
   */
  public void setDataFileName(String string) {
    if (string == null || "".equals(string)) {
      throw new IllegalArgumentException("Data filename must be set");
    }
    dataFile = string;
  }

  /**
   * The lock file in url format.
   *
   * @param string the lock file
   */
  public void setLockFileName(String string) {
    if (string == null || "".equals(string)) {
      throw new IllegalArgumentException("lock filename must be set");
    }
    lockFile = string;
  }

  /**
   * Set the number of attempts to gain a lock.
   *
   * @param i the number of attempts, default is 15.
   */
  public void setMaxAttempts(int i) {
    maxAttempts = i;
  }

  /**
   * @see DataStore#setConfiguration(java.util.Properties)
   */
  @Override
  public void setConfiguration(Properties p) throws DataStoreException {
    super.setConfiguration(p);
    initFromProperties(p);
  }

}
