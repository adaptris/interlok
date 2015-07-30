package com.adaptris.core;


/**
 * <p>
 * Defines behaviour related to sending and receiving <code>Event</code>s using
 * other standard framework components.
 * </p>
 */
public interface EventHandler 
 extends AdaptrisComponent, StateManagedComponent {

  /**
   * <p>
   * Send an <code>Event</code> to the configured default destination.
   * </p>
   * @param evt the <code>Event</code> to send
   * @throws CoreException wrapping any underlying Exceptions
   */
  void send(Event evt) throws CoreException;

  /**
   * <p>
   * Send an <code>Event</code> to the specified destination.
   * </p>
   * @param evt the <code>Event</code> to send
   * @param destination the <code>ProduceDestination</code> to send to
   * @throws CoreException wrapping any underlying Exceptions
   */
  void send(Event evt, ProduceDestination destination) throws CoreException;

  /**
   * <p>
   * Sets the source id for this EventHandler. The source id may be used for routing or replying to events and is generally the
   * unique id of the <code>Adapter</code>.
   * </p>
   * 
   * @param sourceId the source id to be sent as part of the <code>Event</code>
   */
  void registerSourceId(String sourceId);

  /**
   * <p>
   * Retrieve the source id for this EventHandler. The source id may be used for routing or replying to events and is generally the
   * unique id of the <code>Adapter</code>.
   * </p>
   * 
   */
  String retrieveSourceId();
  
}
