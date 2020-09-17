package com.adaptris.interlok.util;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import org.junit.Test;

public class CloserTest extends Closer {

  @Test
  public void testClose_AutoCloseable() throws Exception {
    closeQuietly((AutoCloseable[]) null);
    AutoCloseable mock = mock(AutoCloseable.class);
    closeQuietly(null, mock);
    doThrow(new IOException("Expected")).when(mock).close();
    closeQuietly(mock);
  }
}
