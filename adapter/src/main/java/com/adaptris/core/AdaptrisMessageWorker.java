package com.adaptris.core;


/**
 * <p>
 * Defines behaviour common to <code>AdaptrisMessageConsumer</code> and
 * <code>AdaptrisMessageProducer</code>.
 * </p>
 */
public interface AdaptrisMessageWorker extends AdaptrisComponent,
    MessageEventGenerator {

  /**
   * <p>
   * Sets the <code>AdaptrisConnection</code> this component will use.
   * </p>
   *
   * @param connection the <code>AdaptrisConnection</code> to use
   */
  void registerConnection(AdaptrisConnection connection);

  /**
   * Return this components underlying connection.
   * 
   * @param type the type of connection to cast to.
   * @return the connection
   */
  <T> T retrieveConnection(Class<T> type);

  /**
   * <p>
   * Returns the <code>AdaptrisMessageEncoder</code> to use.
   * </p>
   *
   * @return the <code>AdaptrisMessageEncoder</code> to use
   */
  AdaptrisMessageEncoder getEncoder();

  /**
   * <p>
   * Sets the <code>AdaptrisMessageEncoder</code> to use.
   * </p>
   *
   * @param encoder the <code>AdaptrisMessageEncoder</code> to use
   */
  void setEncoder(AdaptrisMessageEncoder encoder);

  /**
   * <p>
   * Called if a <i>connection exception</i> is encountered. Generally this will
   * be when a polling consumer fails to obtain input, when a producer fails to
   * produce or when some third party thread such as JMS exception listener is
   * invoked.
   * </p>
   *
   * @throws CoreException wrapping underlying Exceptions
   */
  void handleConnectionException() throws CoreException;

  /**
   * <p>
   * Encode the passed message using the configured
   * <code>AdaptrisMessageEncoder</code>. If no AME is configured
   * implementations should return <code>msg.getBytes()</code>.
   * </p>
   *
   * @param msg the <code>AdaptrisMessage</code> to encode
   * @return the <code>AdaptrisMessage</code> encoded as a byte[]
   * @throws CoreException wrapping any that occur
   */
  byte[] encode(AdaptrisMessage msg) throws CoreException;

  /**
   * <p>
   * Decodes the passed byte[] using the configured
   * <code>AdaptrisMessageEncoder</code>. If no AME is configured,
   * implementations should just set the byte[] as the payload of a new message.
   * </p>
   *
   * @param bytes the byte[] to decode
   * @return an <code>AdaptrisMessage</code>
   * @throws CoreException wrapping any that occur
   */
  AdaptrisMessage decode(byte[] bytes) throws CoreException;

  /**
   * @return the messageFactory
   */
  public AdaptrisMessageFactory getMessageFactory();

  /**
   * Set the message factory used when creating AdaptrisMessage instances.
   *
   * @param f the messageFactory to set
   */
  public void setMessageFactory(AdaptrisMessageFactory f);

  /**
   * <p>
   * Returns the optional unique identifier for this Component.
   * </p>
   *
   * @return the unique identifier for this Component
   */
  String getUniqueId();
}
