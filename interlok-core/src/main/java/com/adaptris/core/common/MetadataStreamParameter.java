package com.adaptris.core.common;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import javax.validation.constraints.NotBlank;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.types.InterlokMessage;

public abstract class MetadataStreamParameter {

  @NotBlank
  private String metadataKey;
  @AdvancedConfig
  private String contentEncoding;

  public MetadataStreamParameter() {

  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String key) {
    this.metadataKey = Args.notBlank(key, "metadata key");
  }

  public <T extends MetadataStreamParameter> T withMetadataKey(String e) {
    setMetadataKey(e);
    return (T) this;
  }

  public String getContentEncoding() {
    return contentEncoding;
  }

  public void setContentEncoding(String contentEncoding) {
    this.contentEncoding = contentEncoding;
  }

  public <T extends MetadataStreamParameter> T withContentEncoding(String e) {
    setContentEncoding(e);
    return (T) this;
  }

  public static Charset charset(String charset) {
    return StringUtils.isBlank(charset) ? Charset.defaultCharset() : Charset.forName(charset);
  }

  protected static InputStream toInputStream(InterlokMessage m, String key, String charset) throws InterlokException {
    Args.notBlank(key, "metadataKey");
    String data = m.getMessageHeaders().get(key);
    return new ReaderInputStream(new StringReader(data), charset(charset));
  }
}
