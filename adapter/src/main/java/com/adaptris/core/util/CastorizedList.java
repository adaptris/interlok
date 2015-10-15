/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.adaptris.core.ChannelList;
import com.adaptris.core.WorkflowList;

/**
 * 
 * List implementation for use with castor marshal/unmarshalling.
 * 
 * <p>
 * When collections are unmarshalled, the marshalling implemenation may invoke the getter for the list and directly add to the
 * underlying list rather than calling any add element functions that are available. If we need to proactively manage behaviour when
 * adding elements we can use this list proxy to force it to do things our way.
 * <p>
 * <p>
 * This is used by {@link ChannelList} and {@link WorkflowList} so that referential integrity can be enforced when adding elements
 * etc.
 * </p>
 * 
 * @author lchan
 * 
 */
public class CastorizedList<E> extends AbstractCollection<E> implements List<E> {
  private transient List<E> owner;

  public CastorizedList(List<E> cl) {
    this.owner = cl;
  }

  @Override
  public Iterator<E> iterator() {
    return owner.iterator();
  }

  @Override
  public int size() {
    return owner.size();
  }

  @Override
  public boolean add(E element) {
    return owner.add(element);
  }

  @Override
  public void add(int index, E element) {
    owner.add(index, element);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    return owner.addAll(index, c);
  }

  @Override
  public E get(int index) {
    return owner.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return owner.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return owner.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return owner.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return owner.listIterator(index);
  }

  @Override
  public E remove(int index) {
    return owner.remove(index);
  }

  @Override
  public E set(int index, E element) {
    return owner.set(index, element);
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    return owner.subList(fromIndex, toIndex);
  }
}
