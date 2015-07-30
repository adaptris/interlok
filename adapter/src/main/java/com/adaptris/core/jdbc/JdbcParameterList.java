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
