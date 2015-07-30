package com.adaptris.core.fs;

import com.adaptris.core.CoreException;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ProcessedItemCache} implementation that doesn't cache.
 * 
 * @config fs-no-processed-item-cache
 * @license BASIC
 * @author lchan
 * @author $Author: lchan $
 */
@XStreamAlias("fs-no-processed-item-cache")
public class NoCache implements ProcessedItemCache {

  public NoCache() {
  }

  @Override
  public boolean contains(String key) {
    return false;
  }

  @Override
  public ProcessedItem get(String key) {
    return null;
  }

  @Override
  public void update(ProcessedItem i) {
    return;
  }


  @Override
  public void clear() {
    return;
  }

  @Override
  public int size() {
    return 0;
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#close()
   */
  @Override
  public void close() {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#init()
   */
  @Override
  public void init() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#start()
   */
  @Override
  public void start() throws CoreException {
  }

  /**
   * @see com.adaptris.core.AdaptrisComponent#stop()
   */
  @Override
  public void stop() {
  }

  /**
   * @see com.adaptris.core.LicensedComponent#isEnabled(License)
   */
  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }

  @Override
  public void save() {
  }

  @Override
  public void update(ProcessedItemList list) {
  }

  @Override
  public void evict() {
  }
}
