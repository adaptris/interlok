package com.adaptris.core;

import com.adaptris.util.license.License;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Null implemenation of <code>AdaptrisConnection</code>.
 * </p>
 * 
 * @config null-connection
 */
@XStreamAlias("null-connection")
public class NullConnection extends AdaptrisConnectionImp {

  public NullConnection() {
    super();
    setConnectionErrorHandler(null);
  }

  public NullConnection(String uniqueId) {
    this();
    setUniqueId(uniqueId);
  }

  @Override
  protected void initConnection() throws CoreException {
    ;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#startConnection()
   */
  @Override
  protected void startConnection() throws CoreException {
    ;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#stopConnection()
   */
  @Override
  protected void stopConnection() {
    ;
  }

  /**
   *
   * @see com.adaptris.core.AdaptrisConnectionImp#closeConnection()
   */
  @Override
  protected void closeConnection() {
    ;
  }


  /** @see AdaptrisComponent */
  public boolean isEnabled(License license) throws CoreException {
    return true; // always enabled
  }
}
