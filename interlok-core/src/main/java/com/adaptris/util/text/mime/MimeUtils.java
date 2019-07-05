package com.adaptris.util.text.mime;

import static com.adaptris.util.text.mime.MimeConstants.HEADER_CONTENT_ENCODING;
import static org.apache.commons.lang3.StringUtils.isBlank;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeUtility;

public class MimeUtils {


  public static byte[] encodeData(byte[] data, String encoding, InternetHeaders hdrs)
      throws MessagingException, IOException {
    if (!isBlank(encoding)) {
      hdrs.setHeader(HEADER_CONTENT_ENCODING, encoding);
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (OutputStream encodedOut = MimeUtility.encode(out, encoding)) {
      encodedOut.write(data);
    }
    return out.toByteArray();
  }
}
