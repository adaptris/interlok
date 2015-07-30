package com.adaptris.core;


/**
 * <p>
 * Implemented by classes which generate <i> message lifecycle events</i>.
 * Defines a single method <code>getName</code> which returns the name of
 * the event.
 * </p>
 */
public interface MessageEventGenerator {

  /**
   * <p>
   * Metadata key for confirmation ID.
   * </p>
   */
  String CONFIRMATION_ID_KEY = "adp.confirmation.id";

  /**
   * Create a name for any {@link MleMarker} that is generated.
   *
   *
   * <p>
   * The default implementation is to return the classname of the component in
   * question.
   * </p>
   * * @return the name for any {@link MleMarker} that is generated.
   */
  String createName();

  /**
   * Create a qualifier for any {@link MleMarker} that is generated.
   *
   * <p>
   * The default implementation is to return the unique-id of the component in
   * question, or the empty string if not configured
   * </p>
   *
   * @return the qualifier for any {@link MleMarker} that is generated.
   */
  String createQualifier();

  /**
   * <p>
   * Returns true if this should be considered an 'end-point' for tracking
   * purposes, otherwise false.
   * </p>
   * @return true if this should be considered an 'end-point' for tracking
   * purposes, otherwise false
   */
  boolean isTrackingEndpoint();

  /**
   * <p>
   * Returns true if this event is a confirmation, otherwise false.
   * </p>
   * @return true if this event is a confirmation, otherwise false
   */
  boolean isConfirmation();

}
