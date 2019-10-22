package com.adaptris.core.common;

import java.nio.charset.Charset;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.util.Args;

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
}
