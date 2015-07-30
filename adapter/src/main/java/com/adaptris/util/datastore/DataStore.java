/*
 * $Id: DataStore.java,v 1.11 2004/07/19 13:59:39 lchan Exp $
 */
package com.adaptris.util.datastore;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** DataStore abstract class.
 *  <p>This is a simple interface for persisting data.  The underlying
 *  implementation is reponsible for handling stuff like locks and other stuff
 *  <p>An object within the datastore is considered to be uniquely defined by
 *  its "id" and "type".
 *  @see SimpleDataStore
 *  @author $Author: lchan $
 * <p>
 * In the adapter configuration file this class is aliased as <b>data-store</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 */
public abstract class DataStore {

  /** The Default propety containing the implementation name */
  private static final String PROPERTY_NAME = "datastore.imp";
  /** The configuration */
  private transient Properties configuration = null;

  protected transient Logger logR = LoggerFactory.getLogger(this.getClass().getName());

  /** @see Object#Object() 
   */
  public DataStore() {
  }

  /** Set the configuration for this datastore.
   *  @param p the configuration
   *  @throws DataStoreException if there was an error.
   */
  public void setConfiguration(Properties p) throws DataStoreException {
    configuration = p;
  }

  /** Get the configuration for this datastore.
   *  @return the configuration
   */
  public Properties getConfiguration() {
    return configuration;
  }

  /** Persist the data to the store.
   *  @param id the id
   *  @param type the type
   *  @param obj object stored in the datastore
   *  @throws DataStoreException if an error is encountered
   */
  public abstract void persist(String id, String type, Object obj)
    throws DataStoreException;

  /** Retrieve the data from the datastore.
   *  @param id the id
   *  @param type the type
   *  @return the object stored in the datastore
   *  @throws DataStoreException if an error is encountered
   */
  public abstract Object retrieve(String id, String type)
    throws DataStoreException;

  /** Remove the data from the datastore.
   *  @param id the id
   *  @param type the type
   *  @throws DataStoreException if an error is encountered
   */
  public abstract void remove(String id, String type)
    throws DataStoreException;

  /** Check if the data exists in the store.
   *  @param id the id
   *  @param type the type
   *  @return true if the data exists
   *  @throws DataStoreException if an error is encountered
   */
  public abstract boolean exists(String id, String type)
    throws DataStoreException;
}
