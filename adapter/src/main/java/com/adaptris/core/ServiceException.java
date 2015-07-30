package com.adaptris.core;


/**
 * <p>
 * <code>Exception</code> thrown by <code>Service</code>s.
 * </p>
 */
public class ServiceException extends CoreException {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006032201L;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ServiceException() {
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public ServiceException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public ServiceException(String description) {
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
  public ServiceException(String description, Throwable cause) {
    super(description, cause);
  }
}
