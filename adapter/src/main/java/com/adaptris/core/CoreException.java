package com.adaptris.core;


/**
 * <p>
 * Standard <code>Exception</code> in the <code>core</code> package
 * and sub-packages.
 * </p>
 */
public class CoreException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 2006032201L;
  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public CoreException() {
    super();
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public CoreException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public CoreException(String description) {
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
  public CoreException(String description, Throwable cause) {
    super(description, cause);
  }
}
