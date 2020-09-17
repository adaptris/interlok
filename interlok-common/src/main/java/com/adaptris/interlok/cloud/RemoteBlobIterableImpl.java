package com.adaptris.interlok.cloud;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import com.adaptris.interlok.cloud.RemoteBlob;

/** Abstract implementation that makes creating lazy {@code Iterable<RemoteBlob>} instances a bit easier.
 * 
 */
public abstract class RemoteBlobIterableImpl<T> implements Iterable<RemoteBlob>, Iterator<RemoteBlob> {
  
  private boolean iteratorInvoked = false;
  private RemoteBlob nextBlob = null;
    
  @Override
  public Iterator<RemoteBlob> iterator() {
    if (iteratorInvoked) {
      throw new IllegalStateException("iterator already invoked");
    }
    iteratorInvoked = true;
    iteratorInit();
    return this;
  }

  /** Initialise for iterating.
   * 
   */
  protected abstract void iteratorInit();
  
  /** Return the next storage itme.
   * 
   * @return an optional representing the raw item in storage.
   * @throws NoSuchElementException if there are no items are left to iterate over.
   */
  protected abstract Optional<T> nextStorageItem() throws NoSuchElementException;
  
  /** Convert the storage item into a {@link RemoteBlob} and check for acceptablility.
   * 
   * @param storageItem the storage item.
   * @return an optional wrapping the {@link RemoteBlob}
   */
  protected abstract Optional<RemoteBlob> accept(T storageItem);
  
  @Override
  public boolean hasNext() {
    if (nextBlob == null) {
      nextBlob = nextMatchingBlob();
    }
    return nextBlob != null;
  }

  @Override
  public RemoteBlob next() {
    RemoteBlob ret = nextBlob;
    nextBlob = null;
    return ret;
  }
  
  private RemoteBlob nextMatchingBlob() {
    RemoteBlob blob = null;
    try {
      do {
        blob = nextStorageItem().map((item) -> accept(item)).get().orElse(null);
      } while (blob == null);
    } catch (NoSuchElementException e) {
      return null;
    }
    return blob;
  }

  
}
