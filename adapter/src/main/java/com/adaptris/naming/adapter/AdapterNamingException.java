package com.adaptris.naming.adapter;

import com.adaptris.core.CoreException;

public class AdapterNamingException extends CoreException {

  private static final long serialVersionUID = 201409081134L;

  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public AdapterNamingException() {
    super();
  }

  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public AdapterNamingException(Throwable cause) {
    super(cause);
  }

  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public AdapterNamingException(String description) {
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
  public AdapterNamingException(String description, Throwable cause) {
    super(description, cause);
  }
}
