package com.adaptris.core.jdbc;

import com.adaptris.core.CoreException;

public class JdbcParameterException extends CoreException {

  private static final long serialVersionUID = 201308141508L;
  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public JdbcParameterException() {
    super();
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public JdbcParameterException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public JdbcParameterException(String description) {
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
  public JdbcParameterException(String description, Throwable cause) {
    super(description, cause);
  }

}
