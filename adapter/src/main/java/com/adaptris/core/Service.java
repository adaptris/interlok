package com.adaptris.core;


/**
 * <p>
 * Implementations of <code>Service</code> apply aribtrary functionality to
 * <code>AdaptrisMessage</code>s. It is not the responsibility of
 * implementations of <code>Service</code> to handle multiple
 * <code>Thread</code>s - <code>doService</code> need not be synchronized.
 * </p>
 */
public interface Service extends AdaptrisComponent, MessageEventGenerator, StateManagedComponent {

  /**
   * <p>
   * Apply the service to the message.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to process
   * @throws ServiceException wrapping any underlying <code>Exception</code>s
   */
  void doService(AdaptrisMessage msg) throws ServiceException;

  /**
   * <p>
   * Sets the unique identifier for this <code>Service</code>. These unique
   * identifiers are optional but maybe required by some implementations of
   * <code>ServiceCollection</code>.
   * </p>
   *
   * @param uniqueId this <code>Service</code>'s unique identifier
   */
  void setUniqueId(String uniqueId);

  /**
   * <p>
   * Returns the optional unique identifier for this <code>Service</code>.
   * </p>
   *
   * @return the unique identifier for this <code>Service</code>
   */
  String getUniqueId();

  /**
   * <p>
   * Returns true if the implementation supports branching.
   * </p>
   *
   * @return true if the implementation supports branching
   * @see BranchingServiceCollection
   */
  boolean isBranching();

  /**
   * <p>
   * If true containers should continue and apply the next configured
   * <code>Service</code> even if this <code>Service</code> throws an
   * <code>Exception</code>.
   * </p>
   *
   * @return continueOnFail
   */
  boolean continueOnFailure();

}
