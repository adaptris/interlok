package com.adaptris.core.util;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_EXCEPTION;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.lms.FileBackedMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
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


  /**
   * Check the character set and apply it as the ContentEncoding on the message.
   *
   * @param msg the message
   * @param charEnc the characterEncoding
   * @param passthru if true then just call {@link AdaptrisMessage#setContentEncoding(String)}.
   */
  public static AdaptrisMessage checkCharsetAndApply(final AdaptrisMessage msg, String charEnc,
      final boolean passthru) {
    // we can use a consumer here, because CharsetException is a runtime...
    Optional.ofNullable(charEnc).ifPresent((charsetName) -> {
      if (!passthru) {
        if (Charset.isSupported(charsetName)) {
          msg.setContentEncoding(charsetName);
        } else {
          log.trace("'{}' is not supported, using default, and marking", charsetName);
          msg.addObjectHeader(OBJ_METADATA_EXCEPTION, new UnsupportedCharsetException(charsetName));
        }
      } else {
        msg.setContentEncoding(charsetName);
      }
    });
    return msg;
  }


  /**
   * Get a stacktrace from the message if available.
   *
   * @param msg the message
   * @return An optional wrapping {@code ExceptionUtils#getStackTrace(Throwable)}.
   */
  public static Optional<String> stackTraceAsString(AdaptrisMessage msg) {
    Map hdrs = msg.getObjectHeaders();
    if (hdrs.containsKey(OBJ_METADATA_EXCEPTION)) {
      return Optional
          .ofNullable(ExceptionUtils.getStackTrace((Throwable) hdrs.get(OBJ_METADATA_EXCEPTION)));
    }
    return Optional.empty();
  }
}
