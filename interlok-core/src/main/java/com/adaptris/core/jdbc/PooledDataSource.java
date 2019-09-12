package com.adaptris.core.jdbc;

import java.io.Closeable;
import javax.sql.DataSource;

public interface PooledDataSource extends DataSource, Closeable {

}
