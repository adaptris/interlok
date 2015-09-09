package com.adaptris.core.runtime;

import java.util.Collection;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.adaptris.core.AdapterLifecycleEvent;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.FailedMessageRetrier;
import com.adaptris.core.ProcessingExceptionHandler;

/**
 * MBean specification that allows control of a single adapter.
 */
public interface AdapterManagerMBean extends AdapterComponentMBean, ParentRuntimeInfoComponentMBean, HierarchicalMBean,
    ParentComponentMBean {

  /**
   * Standard Message for a force close notification '{@value} '
   */
  String NOTIF_MSG_FORCE_CLOSE = "Adapter Force Close";

  /**
   * Add an {@link AdaptrisConnection} to the adapter's shared connections.
   * 
   * @param xmlString the string representation of the connection.
   * @return true if the connection was added, false if the connection's unique-id already exists in the list.
   * @throws CoreException wrapping any other exception
   * @throws IllegalStateException if the state of the adapter is not closed.
   * @throws IllegalArgumentException if the connection does not have an unique-id
   */
  boolean addSharedConnection(String xmlString) throws CoreException, IllegalStateException, IllegalArgumentException;

  /**
   * Add an {@link AdaptrisConnection} to the adapter's shared connections and bind it to JNDI.
   * <p>
   * This is primarily for adding shared connections when the Adapter is currently started. Runtime manipulation of the adapter
   * allows you to invoke {@link #addChannel(String)} while the adapter is running. If the {@link Channel} object contains a
   * reference to a shared connection, then you should use this method to add the and bind the shared connection to JNDI ready for
   * use.
   * </p>
   * 
   * @param xmlString the string representation of the connection.
   * @return true if the connection was added, false if the connection's unique-id already exists in the list.
   * @throws CoreException wrapping any other exception
   * @throws IllegalStateException if the state of the adapter is actually closed, in which case you should use
   *           {@link #addSharedConnection(String)} instead..
   * @throws IllegalArgumentException if the connection does not have an unique-id
   */
  boolean addAndBindSharedConnection(String xmlString) throws CoreException, IllegalStateException, IllegalArgumentException;

  /**
   * Remove a connection from the adapter's shared connections.
   * 
   * @param connectionId the connection unique-id to remove.
   * @return true if the connection was removed, false otherwise.
   * @throws CoreException wrapping any other exception
   * @throws IllegalStateException if the state of the adapter is not closed.
   */
  boolean removeSharedConnection(String connectionId) throws CoreException, IllegalStateException;

  /**
   * Check if the associated connectionId is already present in the shared connections.
   * 
   * @param connectionId the connection unique-id to check for
   * @return true if the connection unique-id exists
   * @throws CoreException wrapping any other exception
   */
  boolean containsSharedConnection(String connectionId) throws CoreException;

  /**
   * Get all the connection unique-ids that are currently registered as a shared connection.
   * 
   * @return collection of connectionIds
   * @throws CoreException wrapping any other exception
   */
  Collection<String> getSharedConnectionIds() throws CoreException;

  /**
   * Add a {@link Channel} to this adapter.
   * 
   * @param xmlString the string representation of the channel.
   * @return the ObjectName reference to the newly created ChannelManagerMBean.
   * @throws CoreException wrapping any exception
   * @throws MalformedObjectNameException upon ObjectName errors.
   */
  ObjectName addChannel(String xmlString) throws CoreException, MalformedObjectNameException;

  /**
   * Remove a {@link Channel} from this adapter.
   * 
   * <p>
   * This also removes the associated {@link ChannelManager} and calls {@link #unregisterMBean()}.
   * </p>
   * 
   * @param id the id of the channel to remove.
   * @throws CoreException wrapping any exception
   * @return true if the channel existed and was removed, false otherwise.
   * @throws MalformedObjectNameException upon ObjectName errors.
   */
  boolean removeChannel(String id) throws CoreException, MalformedObjectNameException;

  /**
   * Set the {@link ProcessingExceptionHandler} for this adapter.
   * 
   * @param xmlString the string representation of the error handler.
   * @throws CoreException wrapping any exception
   * @throws IllegalStateException if the state of the adapter is not "Closed"
   */
  void setMessageErrorHandler(String xmlString) throws CoreException;

  /**
   * Set the {@link FailedMessageRetrier} for this adapter.
   * 
   * @param xmlString the string representation of the error handler.
   * @throws CoreException wrapping any exception
   * @throws IllegalStateException if the state of the adapter is not "Closed"
   */
  void setFailedMessageRetrier(String xmlString) throws CoreException;

  /**
   * Validate the license associated with this adapter.
   * 
   * @throws CoreException indicating any errors.
   */
  void checkLicense() throws CoreException;

  /**
   * Get the adapter build version.
   * 
   * @return the adapter build version.
   */
  public String getAdapterBuildVersion();

  /**
   * Get a list of all the modules currently installed for this adapter.
   * 
   * @return list of modules + version numbers.
   */
  public List<String> getModuleVersions();

  /**
   * Send an {@link AdapterLifecycleEvent} to any configured event handlers.
   * 
   * @param event the {@link AdapterLifecycleEvent} to send.
   * @throws CoreException wrapping any exception
   */
  public void sendLifecycleEvent(AdapterLifecycleEvent event) throws CoreException;

  /**
   * Forcibly close the associated adapter.
   * 
   * <p>
   * In the event that there is a concrete {@link AllowsRetriesConnection} that is configured incorrectly, and set with infinite
   * retries; then the adapter will appear to be stuck waiting in the incorrect state (generally, attempting to initialise) when
   * asked nicely to stop. Use this method to forcibly terminate whatever operations are being attempted and to shutdown the
   * adapter.
   * </p>
   * 
   * @throws CoreException
   */
  void forceClose() throws CoreException;
}
