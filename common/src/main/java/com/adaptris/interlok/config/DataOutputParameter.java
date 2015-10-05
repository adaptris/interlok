package com.adaptris.interlok.config;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

public interface DataOutputParameter<T> {

  void insert(T data, InterlokMessage msg) throws InterlokException;
}
