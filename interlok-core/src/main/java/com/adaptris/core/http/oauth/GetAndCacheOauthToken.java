package com.adaptris.core.http.oauth;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.core.cache.CacheExpiry;
import com.adaptris.core.cache.CacheExpiry.Expiry;
import com.adaptris.core.cache.CacheProvider;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.services.cache.CacheConnection;
import com.adaptris.core.services.cache.CheckCacheService;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import net.jodah.expiringmap.ExpirationPolicy;

/**
 * Variation of {@link GetOauthToken} that automatically caches the {@link AccessToken} in the cache of your choosing.
 * 
 * <p>
 * If the 'key' exists in the cache, then that is retrieved, and used. If it does not, then the configured
 * {@link AccessTokenBuilder} is used to generate the access token; which is cached against the specified key. If an expiry is
 * available, then that's used to as the expiry for the token within the cache. Bear in mind that not all caches support a
 * per-item-expiry; {@link ExpiringMapCache} does and by default a {@link CacheConnection} with this configured will be used.
 * </p>
 * <p>
 * Note that previously you would have composed a chain of services to achieve the same thing (probably involving
 * {@link CheckCacheService} or similar); this just simplifies that chain into a single step since you almost certainly want to
 * cache the access token.
 * </p>
 * 
 * @config get-and-cache-oauth-token
 * 
 */
@XStreamAlias("get-and-cache-oauth-token")
@ComponentProfile(summary = "Get a cached OAUTH token or get a token and cache it", since = "3.10.1",
    tag = "service,http,https,oauth",
    recommended = {CacheConnection.class})
@DisplayOrder(order = {"cacheKey", "connection", "accessTokenBuilder", "accessTokenWriter"})
public class GetAndCacheOauthToken extends OauthTokenGetter implements ConnectedService {


  @NotBlank
  @InputFieldHint(expression = true)
  private String cacheKey;

  @NotNull
  @Valid
  private AdaptrisConnection connection;

  public GetAndCacheOauthToken() {
    setConnection(new CacheConnection(new ExpiringMapCache().withExpiration(new TimeInterval(1L, TimeUnit.HOURS))
        .withExpirationPolicy(ExpirationPolicy.ACCESSED)));
  }

  @Override
  public void prepare() throws CoreException {
    super.prepare();
    Args.notNull(getConnection(), "connection");
    Args.notBlank(getCacheKey(), "cache-key");
    LifecycleHelper.prepare(getConnection());
  }

  @Override
  protected void initService() throws CoreException {
    super.initService();
    LifecycleHelper.init(getConnection());
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getConnection());
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getConnection());
    super.stop();
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getConnection());
    super.closeService();
  }


  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      Cache cache = getConnection().retrieveConnection(CacheProvider.class).retrieveCache();
      String key = msg.resolve(getCacheKey());
      AccessToken token = (AccessToken) cache.get(key);
      if (token == null) {
        token = getAccessTokenBuilder().build(msg);
        addToCache(cache, key, token);
      }
      tokenWriterToUse().apply(token, msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  private void addToCache(Cache cache, String key, AccessToken token) throws Exception {
    Optional<Expiry> hasExpiry = CacheExpiry.buildExpiry(token.getExpiry());
    // should be hasExpiry.ifPresentOrElse() once we goto J11...
    if (hasExpiry.isPresent()) {
      TimeInterval expiryInterval = hasExpiry.get().expiresIn();
      log.trace("[{}] will expire in {}", key, expiryInterval);
      cache.put(key, token, hasExpiry.get().expiresIn());
    } else {
      log.trace("Expiry for [{}] taken from cache settings", key);
      cache.put(key, token);
    }
  }


  /**
   * The connection to the cache where tokens will be stored.
   * <p>
   * If not expilicitly configured, will be defaulted to {@link ExpiringMapCache}; 1 Hour + ExpirationPolicy.ACCESSED.
   * </p>
   */
  @Override
  public void setConnection(AdaptrisConnection c) {
    connection = Args.notNull(c, "connection");
  }

  @Override
  public AdaptrisConnection getConnection() {
    return connection;
  }

  public GetAndCacheOauthToken withConnection(AdaptrisConnection c) {
    setConnection(c);
    return this;
  }

  public String getCacheKey() {
    return cacheKey;
  }

  /**
   * Set the key to the cache.
   * 
   * @param cacheKey the key to the cache.
   */
  public void setCacheKey(String cacheKey) {
    this.cacheKey = Args.notBlank(cacheKey, "cacheKey");
  }

  public GetAndCacheOauthToken withCacheKey(String s) {
    setCacheKey(s);
    return this;
  }

}
