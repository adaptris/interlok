package com.adaptris.interlok.cloud;

import java.io.PrintWriter;
import java.util.Collection;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.interlok.util.CloseableIterable;

/**
 * Interface for rendering a list of blobs into an existing message.
 * 
 */
public interface BlobListRenderer {

  /**
   * Render the list of {@link RemoteBlob} instances into the message.
   * 
   * @implNote The default implementation simply writes the name of each remote blob into the
   *           message.
   * @param list the list of {@link RemoteBlob}
   * @param msg the message
   * @throws InterlokException wrapping other exceptions.
   */
  default void render(Iterable<RemoteBlob> list, InterlokMessage msg) throws InterlokException {
    try (CloseableIterable<RemoteBlob> closeable = CloseableIterable.ensureCloseable(list);
        PrintWriter ps = new PrintWriter(msg.getWriter())) {
      for (RemoteBlob blob : closeable) {
        ps.println(blob.getName());
      }
    } catch (Exception e) {
      throw wrapInterlokException(e);
    }
  }

  public static InterlokException wrapInterlokException(String msg, Throwable e) {
    if (e instanceof InterlokException) {
      return (InterlokException) e;
    }
    return new InterlokException(msg, e);
  }

  public static InterlokException wrapInterlokException(Throwable e) {
    return wrapInterlokException(e.getMessage(), e);
  }
}
