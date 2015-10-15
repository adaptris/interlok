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

import java.util.ListIterator;

/**
 * Iterator implementation.
 * <p>
 * The optional operations, {@link ListIterator#add(Object)},
 * {@link ListIterator#set(Object)}, {@link ListIterator#remove()} are not
 * supported.
 * </p>
 *
 * @author lchan
 *
 */
public class UnmodifiableListIterator<E> implements ListIterator<E> {
  private ListIterator<E> baseIterator;

  public UnmodifiableListIterator(ListIterator<E> itr) {
    baseIterator = itr;
  }

  @Override
  public void add(E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNext() {
    return baseIterator.hasNext();
  }

  @Override
  public boolean hasPrevious() {
    return baseIterator.hasPrevious();
  }

  @Override
  public E next() {
    return baseIterator.next();
  }

  @Override
  public int nextIndex() {
    return baseIterator.nextIndex();
  }

  @Override
  public E previous() {
    return baseIterator.previous();
  }

  @Override
  public int previousIndex() {
    return baseIterator.previousIndex();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void set(E e) {
    throw new UnsupportedOperationException();
  }
}
