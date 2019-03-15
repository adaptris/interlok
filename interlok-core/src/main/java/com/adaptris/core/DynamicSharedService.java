package com.adaptris.core;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.NumberUtils;
import com.adaptris.util.TimeInterval;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

/**
 * <p>
 * A Service instance that references a Service made available via {@link SharedComponentList#getServices()}.
 * </p>
 * <p>
 * This differs from a normal {@link SharedService} in that it allows you to {@code resolve} the lookup name using
 * {@link AdaptrisMessage#resolve(String)}. This reference will then be looked up and used for processing. A small cache (currently
 * 16 items) is used to avoid excessive lifecycle overhead for looked up services.
 * </p>
 * 
 * @config dynamic-shared-service
 * 
 */
@XStreamAlias("dynamic-shared-service")
@AdapterComponent
@ComponentProfile(summary = "A Service that refers to another Service configured elsewhere", tag = "service,base")
@DisplayOrder(order = {"uniqueId", "lookupName", "maxEntries", "expiration"})
public class DynamicSharedService extends SharedServiceImpl {

  private static final int DEFAULT_MAX_CACHE_SIZE = 16;
  private static final TimeInterval DEFAULT_EXPIRATION = new TimeInterval(1L, TimeUnit.HOURS);

  private transient ExpiringMap<String, Service> cachedServices;

  @AdvancedConfig
  @InputFieldDefault(value = "16")
  @Min(1)
  private Integer maxEntries;
  @AdvancedConfig
  @InputFieldDefault(value = "1 Hour")
  @Valid
  private TimeInterval expiration;

  public DynamicSharedService() {
  }

  public DynamicSharedService(String lookupName) {
    this();
    this.setLookupName(lookupName);
  }
  
  @Override
  public void init() throws CoreException {
    cachedServices = ExpiringMap.builder().maxSize(maxEntries()).asyncExpirationListener(new ExpiredServiceListener())
        .expirationPolicy(ExpirationPolicy.ACCESSED).expiration(expirationMillis(), TimeUnit.MILLISECONDS).build();
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
    for (Service s : getCache().values()) {
      LifecycleHelper.stopAndClose(s, false);
    }
    getCache().clear();
  }

  private Service resolveAndStart(AdaptrisMessage msg) throws ServiceException {
    Service result = null;
    try {
      String id = msg.resolve(getLookupName());
      if (getCache().containsKey(id)) {
        result = getCache().get(id);
      }
      else {
        result = startService(deepClone((Service) triggerJndiLookup(id)));
        getCache().put(id, result);
      }
      log.trace("Looked up [{}]", id);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
    return result;
  }

  private Service startService(Service s) throws CoreException {
    LifecycleHelper.registerEventHandler(s, eventHandler);
    return LifecycleHelper.initAndStart(s, false);
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    applyService(resolveAndStart(msg), msg);
  }

  @Override
  public boolean isBranching() {
    return false;
  }

  protected Map<String, Service> getCache() {
    return cachedServices;
  }

  public Integer getMaxEntries() {
    return maxEntries;
  }

  public void setMaxEntries(Integer maxEntries) {
    this.maxEntries = maxEntries;
  }

  protected int maxEntries() {
    return NumberUtils.toIntDefaultIfNull(getMaxEntries(), DEFAULT_MAX_CACHE_SIZE);
  }

  public <T extends DynamicSharedService> T withMaxEntries(Integer i) {
    setMaxEntries(i);
    return (T) this;
  }

  public TimeInterval getExpiration() {
    return expiration;
  }

  public void setExpiration(TimeInterval expiration) {
    this.expiration = expiration;
  }

  public <T extends DynamicSharedService> T withExpiration(TimeInterval i) {
    setExpiration(i);
    return (T) this;
  }

  protected long expirationMillis() {
    return TimeInterval.toMillisecondsDefaultIfNull(getExpiration(), DEFAULT_EXPIRATION);
  }

  private class ExpiredServiceListener implements ExpirationListener<String, Service> {

    @Override
    public void expired(String key, Service value) {
      LifecycleHelper.stopAndClose(value, false);
    }

  }

}
