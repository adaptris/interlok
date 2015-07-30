package com.adaptris.fs;


/**
 * <p>
 * Root of all custom <code>Exception</code>s in the <code>fs</code> package
 * and sub-packages.
 * </p>
 */
public class FsException extends Exception {
  
  /**
   * 
   */
  private static final long serialVersionUID = 2009081801L;


  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public FsException() { }

  
  /**
   * <p>
   * Creates a new instance with a reference to a previous 
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public FsException(Exception cause) {
    super(cause);
  }
  
  
  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description of the <code>Exception</code>
   */
  public FsException(String description) {
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
  public FsException(String description, Exception cause) {
    super(description, cause);
  }
} 