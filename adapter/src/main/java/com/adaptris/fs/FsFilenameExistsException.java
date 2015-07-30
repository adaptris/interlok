package com.adaptris.fs;


/**
 * <p>
 * Subclass of <code>FsException</code> indicating that a file has not be found.
 * </p>
 */
public class FsFilenameExistsException extends FsException {
  /**
   * 
   */
  private static final long serialVersionUID = 2009081801L;
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public FsFilenameExistsException() { }


  /**
   * <p>
   * Creates a new instance with a reference to a previous
   * <code>Exception</code>.
   * </p>
   * @param cause a previous, causal <code>Exception</code>
   */
  public FsFilenameExistsException(Exception cause) {
    super(cause);
  }


  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description of the <code>Exception</code>
   */
  public FsFilenameExistsException(String description) {
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
  public FsFilenameExistsException(String description, Exception cause) {
    super(description, cause);
  }
}