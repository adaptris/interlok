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

package com.adaptris.core.services.jdbc;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;

public class StatementParameterList extends AbstractCollection<StatementParameter> implements StatementParameterCollection {

  @AutoPopulated
  @NotNull
  private List<StatementParameter> parameters;
  
  public StatementParameterList() {
    this.setParameters(new ArrayList<StatementParameter>());
  }
  
  public StatementParameterList(List<StatementParameter> parameters) {
    this.setParameters(parameters);
  }

  @Override
  public StatementParameter getParameterByName(String name) {
    for(StatementParameter parameter : this.getParameters()) {
      if((parameter.getName() != null) && (parameter.getName().equals(name)))
        return parameter;
    }
    return null;
  }
  
  @Override
  public boolean add(StatementParameter parameter) {
    return getParameters().add(parameter);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public Iterator<StatementParameter> iterator() {
    return getParameters().iterator();
  }

  @Override
  public int size() {
    return getParameters().size();
  }

  @Override
  public void add(int index, StatementParameter element) {
    getParameters().add(index, element);
  }

  @Override
  public boolean addAll(Collection<? extends StatementParameter> c) {
    return getParameters().addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends StatementParameter> c) {
    return getParameters().addAll(index, c);
  }

  @Override
  public StatementParameter get(int index) {
    return getParameters().get(index);
  }

  @Override
  public int indexOf(Object o) {
    return getParameters().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return getParameters().lastIndexOf(o);
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<StatementParameter> listIterator() {
    return getParameters().listIterator();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<StatementParameter> listIterator(int index) {
    return getParameters().listIterator(index);
  }

  @Override
  public StatementParameter remove(int index) {
    return getParameters().remove(index);
  }

  @Override
  public StatementParameter set(int index, StatementParameter element) {
    return getParameters().set(index, element);
  }

  @Override
  public List<StatementParameter> subList(int fromIndex, int toIndex) {
    return getParameters().subList(fromIndex, toIndex);
  }

  @Override
  public void clear() {
    getParameters().clear();
  }

  public List<StatementParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<StatementParameter> parameters) {
    this.parameters = parameters;
  }

}
