package com.adaptris.core.management.vcs;

import com.adaptris.core.CoreException;

public class VcsException extends CoreException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 201503301304L;
  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public VcsException() {
    super();
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public VcsException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public VcsException(String description) {
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
  public VcsException(String description, Throwable cause) {
    super(description, cause);
  }
  

}
