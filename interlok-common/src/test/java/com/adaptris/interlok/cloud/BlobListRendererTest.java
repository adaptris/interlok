package com.adaptris.interlok.cloud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

public class BlobListRendererTest {

  @Test
  public void testRender() throws Exception {
    InterlokMessage msg = Mockito.mock(InterlokMessage.class);
    StringWriter writer = new StringWriter();
    Mockito.when(msg.getWriter()).thenReturn(writer);
    BlobListRenderer render = new BlobListRenderer() {};
    Collection<RemoteBlob> blobs = createBlobs(10);
    render.render(blobs, msg);
    assertEquals(10, IOUtils.readLines(new StringReader(writer.toString())).size());
  }

  @Test
  public void testRender_Fail() throws Exception {
    InterlokMessage msg = Mockito.mock(InterlokMessage.class);
    StringWriter writer = new StringWriter();
    Mockito.when(msg.getWriter()).thenThrow(new IOException());
    BlobListRenderer render = new BlobListRenderer() {};
    Collection<RemoteBlob> blobs = createBlobs(10);
    try {
      render.render(blobs, msg);
      fail();
    } catch (InterlokException expected) {

    }
  }

  @Test
  public void testWrapException() {
    InterlokException ex = new InterlokException("exception");
    assertEquals(ex, BlobListRenderer.wrapInterlokException(ex));
    assertNotEquals(ex, BlobListRenderer.wrapInterlokException(new Exception("exception")));
  }

  private static Collection<RemoteBlob> createBlobs(int count) {
    List<RemoteBlob> result = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      result.add(new RemoteBlob.Builder().setBucket("bucket").setLastModified(new Date().getTime()).setName("File_" + i)
          .setSize(10L).build());
    }
    return result;
  }
}
