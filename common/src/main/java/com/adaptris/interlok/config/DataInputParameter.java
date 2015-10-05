package com.adaptris.interlok.config;

import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

public interface DataInputParameter<T> {

  T extract(InterlokMessage m) throws InterlokException;
}
