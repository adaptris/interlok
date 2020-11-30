package com.adaptris.core.jdbc;

import java.io.IOException;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class C3P0PooledDataSource extends PooledDataSourceImpl<ComboPooledDataSource> {
  private ComboPooledDataSource wrapped = null;

  public C3P0PooledDataSource(ComboPooledDataSource wrapped) {
    super();
    this.wrapped = wrapped;
  }

  @Override
  public ComboPooledDataSource wrapped() {
    return wrapped;
  }

  @Override
  public void close() throws IOException {
    wrapped().close();
  }

}
