package com.adaptris.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.junit.Test;

import com.adaptris.core.NullService;

public class UnmodifiableListIteratorTest {

  public UnmodifiableListIteratorTest() {
  }

  @Test
  public void testListIterator() {
    ListContainer list = new ListContainer();
    list.addAll(Arrays.asList(new NullService[]
    {
        new NullService(UUID.randomUUID().toString()), new NullService(UUID.randomUUID().toString())
    }));
    assertEquals(2, list.size());
    assertNotNull(list.listIterator(0));
    int count = 0;
    for (ListIterator<NullService> i = list.listIterator(0); i.hasNext();) {
      assertNotNull(i.next());
      count++;
    }
    assertEquals(2, count);
    assertNotNull(list.listIterator());
    count = 0;
    for (ListIterator<NullService> i = list.listIterator(); i.hasNext();) {
      assertNotNull(i.next());
      count++;
    }
    assertEquals(2, count);
  }

  @Test
  public void testIterator() throws Exception {
    ListContainer list = new ListContainer();
    list.addAll(Arrays.asList(new NullService[]
    {
        new NullService(UUID.randomUUID().toString()), new NullService(UUID.randomUUID().toString())
    }));
    assertEquals(2, list.size());
    assertNotNull(list.iterator());
    int count = 0;
    for (Iterator<NullService> i = list.iterator(); i.hasNext();) {
      assertNotNull(i.next());
      count++;
    }
    assertEquals(2, count);

  }

  @Test
  public void testIteratorIsReadOnly() {
    ListContainer list = new ListContainer();
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    list.add(new NullService(UUID.randomUUID().toString()));
    int count = 0;
    for (ListIterator<NullService> i = list.listIterator(); i.hasNext();) {
      switch (count) {
      case 0: {
        assertEquals(-1, i.previousIndex());
        assertFalse(i.hasPrevious());
        assertTrue(i.hasNext());
        break;
      }
      case 5: {
        assertEquals(5, i.nextIndex());
        assertTrue(i.hasPrevious());
        assertFalse(i.hasNext());
        break;
      }
      default: {
        assertEquals(count, i.nextIndex());
        assertNotNull(i.previous());
        assertNotNull(i.next());
        assertTrue(i.hasPrevious());
        assertTrue(i.hasNext());
      }
      }
      i.next();
      count++;
      try {
        i.remove();
        fail();
      }
      catch (UnsupportedOperationException e) {

      }
      try {
        i.add(new NullService());
        fail();
      }
      catch (UnsupportedOperationException e) {

      }
      try {
        i.set(new NullService());
        fail();
      }
      catch (UnsupportedOperationException e) {

      }
    }
  }

  private class ListContainer extends AbstractCollection<NullService> implements List<NullService> {

    private List<NullService> services = new ArrayList<NullService>();

    @Override
    public boolean add(NullService service) {
      return services.add(service);
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Iterator<NullService> iterator() {
      return new UnmodifiableListIterator<NullService>(services.listIterator());
    }

    @Override
    public int size() {
      return services.size();
    }

    @Override
    public void add(int index, NullService element) {
      services.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends NullService> c) {
      return services.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends NullService> c) {
      return services.addAll(index, c);
    }

    @Override
    public NullService get(int index) {
      return services.get(index);
    }

    @Override
    public int indexOf(Object o) {
      return services.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
      return services.lastIndexOf(o);
    }

    @Override
    public ListIterator<NullService> listIterator() {
      return new UnmodifiableListIterator<NullService>(services.listIterator());
    }

    @Override
    public ListIterator<NullService> listIterator(int index) {
      return new UnmodifiableListIterator<NullService>(services.listIterator(index));
    }

    @Override
    public NullService remove(int index) {
      return services.remove(index);
    }

    @Override
    public NullService set(int index, NullService element) {
      return services.set(index, element);
    }

    @Override
    public List<NullService> subList(int fromIndex, int toIndex) {
      return services.subList(fromIndex, toIndex);
    }

    @Override
    public void clear() {
      services.clear();
    }

  }
}
