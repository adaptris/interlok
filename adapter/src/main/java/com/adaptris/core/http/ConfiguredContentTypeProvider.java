package com.adaptris.core.http;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a static content type.
 * <p>
 * Note that the content type character set will be derived from
 * {@link AdaptrisMessage#getCharEncoding()} so configuring a mime type of {@code text/xml} when the
 * message has a char encoding of {@code UTF-8} will return {@code text/xml; charset=UTF-8}
 * </p>
 * 
 * @config http-configured-content-type-provider
 */
@XStreamAlias("http-configured-content-type-provider")
public class ConfiguredContentTypeProvider extends ContentTypeProviderImpl {

  @NotBlank
  @AutoPopulated
  private String mimeType;

  public ConfiguredContentTypeProvider() {
    setMimeType("text/plain");
  }

  public ConfiguredContentTypeProvider(String type) {
    this();
    setMimeType(type);
  }

  @Override
  public String getContentType(AdaptrisMessage msg) {
    return build(getMimeType(), msg.getCharEncoding());
  }


  public String getMimeType() {
    return mimeType;
  }

  /**
   * Set the base content type.
   * 
   * @param type the base content type; defaults to text/plain
   */
  public void setMimeType(String type) {
    this.mimeType = Args.notBlank(type, "Mime Type");
  }

}
