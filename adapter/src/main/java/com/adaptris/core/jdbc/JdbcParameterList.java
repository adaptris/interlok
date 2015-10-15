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

package com.adaptris.core.jdbc;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;

public abstract class JdbcParameterList<T> extends AbstractCollection<T> implements ParameterList<T> {

  public abstract List<T> getParameters();
  
  @Override
  public Iterator<T> iterator() {
    return this.getParameters().iterator();
  }

  @Override
  public int size() {
    return this.getParameters().size();
  }

  @Override
  public T getByName(String name) {
    T result = null;
    for(T param : this.getParameters()) {
      if(((JdbcParameter) param).getName().equals(name)) {
        result = param;
        break;
      }
    }
    return result;
  }
  
  @Override
  public T getByOrder(int order) {
    T result = null;
    for(T param : this.getParameters()) {
      if(((JdbcParameter) param).getOrder() == order) {
        result = param;
        break;
      }
    }
    return result;
  }
  
  @SuppressWarnings("unchecked")
  public boolean add(Object object) {
    this.getParameters().add((T) object);
    return true;
  }
}
