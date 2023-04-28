package com.adaptris.core.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;

public class CacheDefaultMethodsTest implements Cache {

  @Test
  public void testDefaultPutStringObject() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      try {
        LifecycleHelper.initAndStart(this);
        put("hello", new Object());
      } finally {
        LifecycleHelper.stopAndClose(this);
      }
    });
  }

  @Test
  public void testDefaultGetKeys() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      try {
        LifecycleHelper.initAndStart(this);
        getKeys();
      } finally {
        LifecycleHelper.stopAndClose(this);
      }
    });
  }

  @Test
  public void testDefaultClear() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      try {
        LifecycleHelper.initAndStart(this);
        clear();
      } finally {
        LifecycleHelper.stopAndClose(this);
      }
    });
  }

  @Test
  public void testDefaultSize() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      try {
        LifecycleHelper.initAndStart(this);
        size();
      } finally {
        LifecycleHelper.stopAndClose(this);
      }
    });
  }

  @Test
  public void testDefaultPutSerializable_WithExpiration() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      TimeInterval expiry = new TimeInterval(250L, TimeUnit.MILLISECONDS);
      try {
        LifecycleHelper.initAndStart(this);
        put("hello", "", expiry);
      } finally {
        LifecycleHelper.stopAndClose(this);
      }
    });
  }

  @Test
  public void testDefaultPutObject_WithExpiration() throws Exception {
    Assertions.assertThrows(UnsupportedOperationException.class, () -> {
      TimeInterval expiry = new TimeInterval(250L, TimeUnit.MILLISECONDS);
      try {
        LifecycleHelper.initAndStart(this);
        put("hello", new Object(), expiry);
      } finally {
        LifecycleHelper.stopAndClose(this);
      }
    });
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
