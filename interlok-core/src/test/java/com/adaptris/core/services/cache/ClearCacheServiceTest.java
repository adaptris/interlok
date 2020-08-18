package com.adaptris.core.services.cache;

import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.cache.Cache;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class ClearCacheServiceTest {

  @Test
  public void testDoService() throws Exception {
    Cache mockCache = Mockito.mock(Cache.class);
    CacheConnection cache = new CacheConnection(mockCache);
    ClearCacheService service = new ClearCacheService().withConnection(cache);
    ExampleServiceCase.execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  @Test
  public void testDoService_Quietly() throws Exception {
    Cache mockCache = Mockito.mock(Cache.class);
    Mockito.doThrow(new UnsupportedOperationException()).when(mockCache).clear();
    CacheConnection cache = new CacheConnection(mockCache);
    ClearCacheService service = new ClearCacheService().withIgnoreUnsupported(true).withConnection(cache);
    ExampleServiceCase.execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  @Test(expected = ServiceException.class)
  public void testDoService_NotQuietly() throws Exception {
    Cache mockCache = Mockito.mock(Cache.class);
    Mockito.doThrow(new UnsupportedOperationException()).when(mockCache).clear();
    CacheConnection cache = new CacheConnection(mockCache);
    ClearCacheService service = new ClearCacheService().withConnection(cache);
    ExampleServiceCase.execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }

  @Test(expected = ServiceException.class)
  public void testDoService_Exception() throws Exception {
    Cache mockCache = Mockito.mock(Cache.class);
    Mockito.doThrow(new CoreException()).when(mockCache).clear();
    CacheConnection cache = new CacheConnection(mockCache);
    ClearCacheService service = new ClearCacheService().withIgnoreUnsupported(true).withConnection(cache);
    ExampleServiceCase.execute(service, AdaptrisMessageFactory.getDefaultInstance().newMessage());
  }
}
