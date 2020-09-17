package com.adaptris.interlok.cloud;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

public class RemoteBlobIterableTest {

  @Test
  public void testIterator() throws Exception {
    int first = 10;
    int second = 10;
    MyRemoteBlobIterable itr = new MyRemoteBlobIterable(first, second);
    Iterator<RemoteBlob> i = itr.iterator();
    int size = 0;
    i.hasNext();
    while (i.hasNext()) {
      i.next();
      size ++;
    }
    assertEquals(first + second -1 , size);
  }
  
  @Test(expected=IllegalStateException.class)
  public void testIterator_Double() throws Exception {  
    MyRemoteBlobIterable itr = new MyRemoteBlobIterable(1, 0);
    itr.iterator();
    itr.iterator();
  }
  
  
  private class MyRemoteBlobIterable extends RemoteBlobIterableImpl<RemoteBlob> {

    private List<RemoteBlob> first = new ArrayList<>();
    private List<RemoteBlob> second = new ArrayList<>();
    private Iterator<RemoteBlob> currentIterator = null;
    private boolean exhausted = false;
    private boolean rejectedOnce = false;
    
    MyRemoteBlobIterable(int count1, int count2) {
      for (int i = 0; i < count1; i++) {
        first.add(
            new RemoteBlob.Builder().setBucket("bucket").setLastModified(System.currentTimeMillis())
                .setSize(-1).setName(UUID.randomUUID().toString()).build());
      }
      for (int i = 0; i < count2; i++) {
        second.add(
            new RemoteBlob.Builder().setBucket("bucket").setLastModified(System.currentTimeMillis())
                .setSize(-1).setName(UUID.randomUUID().toString()).build());
      }
    }
    
    
    @Override
    protected void iteratorInit() {
      currentIterator = first.iterator();
    }

    @Override
    protected Optional<RemoteBlob> nextStorageItem() throws NoSuchElementException {
      if (!currentIterator.hasNext()) {
        advanceToNextPage();
      } 
      return Optional.ofNullable(currentIterator.next());        
    }

    private void advanceToNextPage() throws NoSuchElementException {
      if (exhausted) {
        throw new NoSuchElementException();
      }
      currentIterator = second.iterator();
      exhausted = true;
    }
    
    @Override
    protected Optional<RemoteBlob> accept(RemoteBlob storageItem) {
      if (!rejectedOnce) {
        rejectedOnce = true;
        return Optional.empty();
      }
      return Optional.of(storageItem);
    }
  }


}
