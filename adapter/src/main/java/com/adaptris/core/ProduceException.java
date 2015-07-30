package com.adaptris.core;


/**
 * <p>
 * <code>Exception</code> thrown by <code>AdaptrisMessageProducer</code>s.
 * </p>
 */
public class ProduceException extends CoreException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006032201L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ProduceException() {
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public ProduceException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public ProduceException(String description) {
    super(description);
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code> and a description of the <code>Exception</code>.
   * </p>
   * @param description of the <code>Exception</code>
   * @param cause previous <code>Exception</code>
   */
  public ProduceException(String description, Throwable cause) {
    super(description, cause);
  }
}
