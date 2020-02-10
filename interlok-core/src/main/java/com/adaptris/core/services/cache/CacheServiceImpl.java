package com.adaptris.core.services.cache;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.SharedConnection;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.CacheProvider;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;

public abstract class CacheServiceImpl extends ServiceImp implements ConnectedService {
  @Valid
  @NotNull
  private AdaptrisConnection connection;


  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(getConnection(), "connection");
      LifecycleHelper.prepare(getConnection());
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  public void closeService() {
    LifecycleHelper.close(getConnection());
  }

  @Override
  public void initService() throws CoreException {
    LifecycleHelper.init(getConnection());

  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getConnection());
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getConnection());
  }

  protected Cache retrieveCache() {
    return getConnection().retrieveConnection(CacheProvider.class).retrieveCache();
  }

  @Override
  public AdaptrisConnection getConnection() {
    return connection;
  }

  /**
   * Set the connection associated with this cache service.
   * 
   * @see CacheConnection
   * @see SharedConnection
   */
  @Override
  public void setConnection(AdaptrisConnection cacheConnection) {
    this.connection = Args.notNull(cacheConnection, "connection");
  }

  public <T extends CacheServiceImpl> T withConnection(AdaptrisConnection c) {
    setConnection(c);
    return (T) this;
  }

}
