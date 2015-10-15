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

package com.adaptris.core.runtime;

import java.util.Collection;

import com.adaptris.core.CoreException;

/**
 * Basic interface of MBeans that contain child member components.
 *
 * @author lchan
 */
public interface ParentComponent<S> extends ParentComponentMBean {

  /**
   * Create an ObjectName key/value pair hierarchy.
   *
   * @return a hierarchy based on the tree.
   */
  String createObjectHierarchyString();


  /**
   * Add a a child to this parent.
   *
   * @param wmb a channel manager
   * @return true if the underlying components were modified.
   * @throws CoreException wrapping any underlying exception
   * @see java.util.Set#add(Object)
   */
  boolean addChild(S wmb) throws CoreException;

  /**
   * Remove a Child from this parent.
   *
   * @param wmb a channel manager
   * @return true if the component existed as a child
   * @throws CoreException wrapping any underlying exception
   * @see java.util.Set#remove(Object)
   */
  boolean removeChild(S wmb) throws CoreException;;

  /**
   * Add some children to this parent (optional operation).
   *
   * @param coll a collection of items to be added.
   * @return true if the underlying components were modified.
   * @throws CoreException wrapping any underlying exception
   * @see java.util.Set#addAll(Collection)
   * @throws UnsupportedOperationException - if the addChildren operation is not supported.
   */
  boolean addChildren(Collection<S> coll) throws CoreException, UnsupportedOperationException;

  /**
   * Remove some children from this parent.
   *
   * @param coll a collection of items to be removed.
   * @return true if the underlying components were modified.
   * @throws CoreException wrapping any underlying exception
   * @see java.util.Set#addAll(Collection)
   */
  boolean removeChildren(Collection<S> coll) throws CoreException;

  /**
   * Get all the descendants of this ParentComponent implementation.
   *
   * @return all the children, and children's children, etc...
   */
  Collection<BaseComponentMBean> getAllDescendants();

  /**
   * Notifies a parent that a child component has been updated.
   * 
   * @throws CoreException
   * 
   */
  void childUpdated() throws CoreException;

  // It seems to me that if we're going to refer to children, then we should have methods like orphan, take_into_fostercase
  // and callSocialServices().
}
