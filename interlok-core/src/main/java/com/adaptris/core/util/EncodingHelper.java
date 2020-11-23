package com.adaptris.core.util;

import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.internet.MimeUtility;

import com.adaptris.util.text.mime.MimeConstants;
import com.adaptris.validation.constraints.ConfigDeprecated;

public class EncodingHelper {

  /**
   * Standard supported encodings
   */
  public enum Encoding {
    @ConfigDeprecated(removalVersion = "4.0.0", message = "Use Base64_MIME / Base64_URL / Base64_Basic instead", groups = Deprecated.class)
    Base64 {
      @Override
      public OutputStream wrap(OutputStream orig) throws Exception {
        return MimeUtility.encode(orig, MimeConstants.ENCODING_BASE64);
      }

      @Override
      public InputStream wrap(InputStream orig) throws Exception {
        return MimeUtility.decode(orig, MimeConstants.ENCODING_BASE64);
      }
    },
    /**
     * Base64 using the MIME type base64 scheme.
     * <p>
     * Uses {@link java.util.Base64} under the covers
     * </p>
     */
    MIME_Base64 {
      @Override
      public OutputStream wrap(OutputStream orig) throws Exception {
        return java.util.Base64.getEncoder().wrap(orig);
      }

      @Override
      public InputStream wrap(InputStream orig) throws Exception {
        return java.util.Base64.getDecoder().wrap(orig);
      }
    },
    /**
     * 'quoted-printable' encoding MIME style.
     * <p>
     * Uses {@link MimeUtility} as the encoder / decoder.
     * </p>
     */
    Quoted_Printable {
      @Override
      public OutputStream wrap(OutputStream orig) throws Exception {
        return MimeUtility.encode(orig, MimeConstants.ENCODING_QUOTED);
      }

      @Override
      public InputStream wrap(InputStream orig) throws Exception {
        return MimeUtility.decode(orig, MimeConstants.ENCODING_QUOTED);
      }

    },
    /**
     * 'uuencode' encoding MIME style.
     * <p>
     * Uses {@link MimeUtility} as the encoder / decoder.
     * </p>
     */
    UUEncode {
      @Override
      public OutputStream wrap(OutputStream orig) throws Exception {
        return MimeUtility.encode(orig, MimeConstants.ENCODING_UUENCODE);
      }

      @Override
      public InputStream wrap(InputStream orig) throws Exception {
        return MimeUtility.decode(orig, MimeConstants.ENCODING_UUENCODE);
      }
    },
    /**
     * Base64 using URL and Filename safe type base64 scheme.
     * <p>
     * Uses {@link java.util.Base64} under the covers
     * </p>
     */
    URL_Base64 {
      @Override
      public OutputStream wrap(OutputStream orig) throws Exception {
        return java.util.Base64.getUrlEncoder().wrap(orig);
      }

      @Override
      public InputStream wrap(InputStream orig) throws Exception {
        return java.util.Base64.getUrlDecoder().wrap(orig);
      }
    },
    /**
     * Base64 using the Basic type base64 encoding scheme.
     * <p>
     * Uses {@link java.util.Base64} under the covers
     * </p>
     */
    Basic_Base64 {
      @Override
      public OutputStream wrap(OutputStream orig) throws Exception {
        return java.util.Base64.getEncoder().wrap(orig);
      }

      @Override
      public InputStream wrap(InputStream orig) throws Exception {
        return java.util.Base64.getDecoder().wrap(orig);
      }
    },
    /**
     * No Encoding.
     *
     */
    None {
      @Override
      public OutputStream wrap(OutputStream orig) {
        return orig;
      }

      @Override
      public InputStream wrap(InputStream orig) {
        return orig;
      }
    };

    public abstract OutputStream wrap(OutputStream out) throws Exception;

    public abstract InputStream wrap(InputStream in) throws Exception;
  }


  /**
   * Just the supported Base64 styles which are available via {@link java.util.Base64}
   *
   */
  public enum Base64Encoding {
    /**
     * Base64 sing the MIME type base64 scheme.
     */
    MIME {
      @Override
      public java.util.Base64.Decoder decoder() {
        return java.util.Base64.getMimeDecoder();
      }

      @Override
      public java.util.Base64.Encoder encoder() {
        return java.util.Base64.getMimeEncoder();
      }

    },
    /**
     * Base64 using URL and Filename safe type base64 scheme.
     */
    URL {
      @Override
      public java.util.Base64.Decoder decoder() {
        return java.util.Base64.getUrlDecoder();
      }

      @Override
      public java.util.Base64.Encoder encoder() {
        return java.util.Base64.getUrlEncoder();
      }
    },
    /**
     * Base64 using the Basic type base64 encoding scheme.
     */
    BASIC {
      @Override
      public java.util.Base64.Decoder decoder() {
        return java.util.Base64.getDecoder();
      }

      @Override
      public java.util.Base64.Encoder encoder() {
        return java.util.Base64.getEncoder();
      }

    };

    public abstract java.util.Base64.Decoder decoder();

    public abstract java.util.Base64.Encoder encoder();
  }
}
