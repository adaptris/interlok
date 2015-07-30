package com.adaptris.http;

/**
 * <p>
 * Root of all custom <code>Exception</code>s in the <code>http</code> package
 * and sub-packages.
 * </p>
 */
public class HttpException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -2006020601L;


  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public HttpException() { }


  /**
   * <p>
   * Creates a new instance with a reference to a previous
   * <code>Throwable</code>.
   * </p>
   * @param cause a previous, causal <code>Throwable</code>
   */
  public HttpException(Throwable cause) {
    super(cause);
  }


  /**
   * <p>
   * Creates a new instance with a description of the <code>Exception</code>.
   * </p>
   * @param description description of the <code>Exception</code>
   */
  public HttpException(String description) {
    super(description);
  }


  /**
   * <p>
   * Creates a new instance with a reference to a previous
   * <code>Throwable</code> and a description of the <code>Exception</code>.
   * </p>
   * @param description of the <code>Exception</code>
   * @param cause previous <code>Throwable</code>
   */
  public HttpException(String description, Throwable cause) {
    super(description, cause);
  }
} 