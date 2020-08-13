package com.adaptris.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.lms.FileBackedMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageHelper {

  /**
   * Copy the payload from the src to the destination message.
   *
   * <p>
   * Has the specific behaviour where if both message implementations are a
   * {@link FileBackedMessage} then the src message is added as object metadata to the destination
   * message to avoid garbage collection scoping issues (since temporary files will be removed on
   * gc).
   * </p>
   *
   */
  public static void copyPayload(AdaptrisMessage src, AdaptrisMessage dest) throws IOException {
    if (BooleanUtils
        .and(new boolean[] {src instanceof FileBackedMessage, dest instanceof FileBackedMessage})) {
      ((FileBackedMessage) dest).initialiseFrom(((FileBackedMessage) src).currentSource());
      // INTERLOK-2189 stop the reply from going out of scope.
      ((FileBackedMessage) dest).addObjectHeader(src.getUniqueId(), src);
    } else {
      copyPayloadStream(src, dest);
    }
  }

  /**
   * Copy the payload from the src to the destination message.
   *
   *
   */
  public static void copyPayloadStream(AdaptrisMessage src, AdaptrisMessage dest)
      throws IOException {
    try (InputStream in = src.getInputStream(); OutputStream out = dest.getOutputStream()) {
      IOUtils.copy(in,  out);
    }
  }

}
