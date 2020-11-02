package com.adaptris.core.http.jetty.retry;

import static org.junit.Assert.assertEquals;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.cloud.RemoteBlob;

public class ReportBuilderTest {

  @Test
  public void testBuild() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    ReportBuilder builder = new ReportBuilder();
    try {
      LifecycleHelper.initAndStart(builder);
      builder.build(listFiles(10), msg);
      try (InputStream in = msg.getInputStream()) {
        List lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
        assertEquals(10, lines.size());
      }
    } finally {
      LifecycleHelper.stopAndClose(builder);
    }
  }

  private Iterable<RemoteBlob> listFiles(int count) {
    List<RemoteBlob> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(new RemoteBlob.Builder().setBucket("bucket").setName("file" + i).setSize(i)
          .setLastModified(System.currentTimeMillis()).build());
    }
    return list;
  }
}
