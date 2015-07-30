package com.adaptris.core.stubs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.lms.FileBackedMessage;

public class MessageHelper {

  public static AdaptrisMessage createMessage(AdaptrisMessageFactory factory, String filename) throws IOException {
    AdaptrisMessage m = factory.newMessage();
    if (m instanceof FileBackedMessage) {
      ((FileBackedMessage) m).initialiseFrom(new File(filename));
    }
    else {
      OutputStream out = null;
      InputStream in = null;
      try {
        in = new FileInputStream(new File(filename));
        out = m.getOutputStream();
        IOUtils.copy(in, out);
      }
      finally {
        IOUtils.closeQuietly(out);
        IOUtils.closeQuietly(in);
      }
    }
    return m;
  }

  public static AdaptrisMessage createMessage(String filename) throws IOException {
    return createMessage(new DefaultMessageFactory(), filename);
  }

}
