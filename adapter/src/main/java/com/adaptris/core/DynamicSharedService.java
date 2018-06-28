package com.adaptris.core;

import java.util.LinkedHashMap;
import java.util.Map;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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
@DisplayOrder(order = {"uniqueId", "lookupName"})
public class DynamicSharedService extends SharedServiceImpl {

  private static final int DEFAULT_MAX_CACHE_SIZE = 16;
  
  private transient Map<String, Service> cachedServices = new DumbServiceCache();

  public DynamicSharedService() {
  }

  public DynamicSharedService(String lookupName) {
    this();
    this.setLookupName(lookupName);
  }
  
  @Override
  public void init() throws CoreException {
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
    for (Service s : cachedServices.values()) {
      LifecycleHelper.stopAndClose(s);
    }
    cachedServices.clear();
  }

  private Service resolveAndStart(AdaptrisMessage msg) throws ServiceException {
    Service result = null;
    try {
      String id = msg.resolve(getLookupName());
      if (cachedServices.containsKey(id)) {
        result = cachedServices.get(id);
      }
      else {
        result = startService(deepClone((Service) triggerJndiLookup(id)));
        cachedServices.put(id, result);
      }
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
    Service s = resolveAndStart(msg);
    try {
      s.doService(msg);
      msg.addEvent(s, true);
    }
    catch (Exception e) {
      msg.addEvent(s, false);
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  Map<String, Service> getCache() {
    return cachedServices;
  }

  int maxCacheSize() {
    return DEFAULT_MAX_CACHE_SIZE;
  }

  private class DumbServiceCache extends LinkedHashMap<String, Service> {

    private static final long serialVersionUID = 2017080201L;

    public DumbServiceCache() {
      super(DEFAULT_MAX_CACHE_SIZE, 0.75f, true);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Service> eldest) {
      if (size() > maxCacheSize()) {
        LifecycleHelper.stopAndClose(eldest.getValue());
        return true;
      }
      return false;
    }
  }

  @Override
  public boolean isBranching() {
    return false;
  }

}
