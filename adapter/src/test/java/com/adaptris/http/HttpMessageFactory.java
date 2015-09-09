package com.adaptris.http;

/** The HttpMessageFactory.
 *  <p>This class is used to create HttpMessage instances.
 */
public abstract class HttpMessageFactory {

  private static HttpMessageFactory defaultfactory = null;
  /** Creates a new instance of HttpMessageFactory */
  protected HttpMessageFactory() {
  }

  /** Get the default implementation of the factory.
   *  @return a HttpMessageFactory
   */
  public static final HttpMessageFactory getDefaultInstance() {
    if (defaultfactory == null) {
      defaultfactory = new HttpDefMessageFactory();
    }
    return defaultfactory;
  }

  /** Create a HttpMessage
   *  @return a HttpMessage
   *  @throws HttpException on error.
   */
  public abstract HttpMessage create() throws HttpException;

  private static class HttpDefMessageFactory extends HttpMessageFactory {
    public HttpMessage create() throws HttpException {
      return (new SimpleHttpMsg());
    }
  }
}
