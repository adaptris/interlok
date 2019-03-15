package com.adaptris.core.cache;

import java.io.Serializable;

import org.junit.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;

public class CacheDefaultMethodsTest implements Cache {


  @Test(expected = UnsupportedOperationException.class)
  public void testDefaultPutStringObject() throws Exception {
    try {
      LifecycleHelper.initAndStart(this);
      put("hello", new Object());
    } finally {
      LifecycleHelper.stopAndClose(this);
    }
  }


  @Test(expected = UnsupportedOperationException.class)
  public void testDefaultGetKeys() throws Exception {
    try {
      LifecycleHelper.initAndStart(this);
      getKeys();
    } finally {
      LifecycleHelper.stopAndClose(this);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDefaultClear() throws Exception {
    try {
      LifecycleHelper.initAndStart(this);
      clear();
    } finally {
      LifecycleHelper.stopAndClose(this);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDefaultSize() throws Exception {
    try {
      LifecycleHelper.initAndStart(this);
      size();
    } finally {
      LifecycleHelper.stopAndClose(this);
    }
  }

  @Override
  public void put(String key, Serializable value) throws CoreException {
  }

  @Override
  public Object get(String key) throws CoreException {
    return null;
  }

  @Override
  public void remove(String key) throws CoreException {
  }

}
