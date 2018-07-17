package com.adaptris.core.services.cache;

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.cache.Cache;

public abstract class CacheServiceExample extends ServiceCase {

  private static final String BASE_DIR_KEY = "CacheServiceExamples.baseDir";
  protected static final String HYPHEN = "-";

  public CacheServiceExample() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected List retrieveObjectsForSampleConfig() {
    List<Object> result = new ArrayList<Object>();
    for (CacheExampleImplementation c : getExampleCacheImplementations()) {
      for (CacheExampleServiceGenerator gen : getExampleGenerators()) {
        Service service = gen.createExampleService();
        navigateTo(service).setConnection(new CacheConnection(c.createCacheImplementation()));
        result.add(service);
      }
    }
    return result;
  }

  @Override
  protected String getExampleCommentHeader(Object object) {
    return super.getExampleCommentHeader(object) + getImplementation(navigateTo(object)).getXmlHeader();
  }

  @Override
  protected String createBaseFileName(Object object) {
    CacheServiceBase p = navigateTo(object);
    return super.createBaseFileName(p) + HYPHEN
        + ((CacheConnection) p.getConnection()).getCacheInstance().getClass().getSimpleName();
  }

  protected CacheServiceBase navigateTo(Object parent) {
    if (parent instanceof BranchingServiceCollection) {
      return navigateTo((BranchingServiceCollection) parent);
    }
    return (CacheServiceBase) parent;
  }

  protected CacheServiceBase navigateTo(BranchingServiceCollection coll) {
    CacheServiceBase baseService = null;
    for (Service s : coll.getServices()) {
      if (s.getUniqueId().equals(coll.getFirstServiceId())) {
        baseService = (CacheServiceBase) s;
        break;
      }
    }
    return baseService;
  }

  protected abstract CacheExampleImplementation getImplementation(CacheServiceBase obj);

  protected abstract Iterable<CacheExampleServiceGenerator> getExampleGenerators();

  protected abstract Iterable<CacheExampleImplementation> getExampleCacheImplementations();

  public interface CacheExampleImplementation {
    Cache createCacheImplementation();

    String getXmlHeader();

  }

  @FunctionalInterface
  public interface CacheExampleServiceGenerator {
    Service createExampleService();
  }

}
