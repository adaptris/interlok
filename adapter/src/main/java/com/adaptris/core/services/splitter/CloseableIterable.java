package com.adaptris.core.services.splitter;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/**
 * This Iterable exists for the purpose of being able to iterate over a list of indeterminate size
 * (possibly too large for memory), while still guaranteeing that whatever resource is being held (like 
 * a Stream) will be closed when iteration finishes (or it goes out of scope).
 * 
 * @param <E>
 */
public interface CloseableIterable<E> extends Closeable, Iterable<E> {
  public static final CloseableIterableFactory FACTORY = new CloseableIterableFactory();
}

/**
 * Helps create CloseableIterables from regular Iterables. This class only exists because 
 * interfaces cannot yet have static methods in Java 7. We'll have to wait for Java 8 to
 * remove this class.
 */
class CloseableIterableFactory {
  /**
   * Make sure that the Iterable is Closeable. If it's already Closeable, just return it. 
   * 
   * @param iter
   * @return
   */
  public <E> CloseableIterable<E> ensureCloseable(final Iterable<E> iter) {
    if(iter instanceof CloseableIterable) {
      return (CloseableIterable<E>)iter;
    }
    
    return new CloseableIterable<E>() {
      @Override
      public void close() throws IOException {
        // No-op
      }

      @Override
      public Iterator<E> iterator() {
        return iter.iterator();
      }
    };
  }
}