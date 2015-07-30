package com.adaptris.http.legacy;

import com.adaptris.core.CoreException;
import com.adaptris.core.NullConnection;
import com.adaptris.http.HttpClient;
import com.adaptris.http.HttpClientTransport;
import com.adaptris.http.HttpException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A Produce Connection for HTTP.
 * <p>
 * This extends <code>NullConnection</code> intentionally
 * <p>
 * <strong>Requires an HTTP license</strong>
 * </p>
 * * <p>
 * In the adapter configuration file this class is aliased as <b>http-produce-connection</b> which is the preferred alternative to the
 * fully qualified classname when building your configuration.
 * </p>
 
 * @author lchan
 * @author $Author: lchan $
 * @deprecated since 2.9.0 use {@link com.adaptris.core.http.JdkHttpProducer} instead
 */
@Deprecated
@XStreamAlias("http-produce-connection")
public class HttpProduceConnection extends NullConnection implements
    HttpClientConnection {

  /**
   * @see com.adaptris.core.AdaptrisComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  /**
   * @see HttpClientConnection#initialiseClient(java.lang.String)
   */
  @Override
  public HttpClientTransport initialiseClient(String url) throws HttpException {
    return new HttpClient(url);
  }

}
