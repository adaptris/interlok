package com.adaptris.core.http;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a static content type.
 * <p>
 * Note that the content type character set if derived from
 * {@link AdaptrisMessage#getCharEncoding()} so configuring a mime type of {@code text/xml} when the
 * message has a char encoding of {@code UTF-8} will return {@code text/xml; charset="UTF-8"}
 * </p>
 * 
 * @config http-configured-content-type-provider
 */
@XStreamAlias("http-configured-content-type-provider")
public class ConfiguredContentTypeProvider extends ContentTypeProviderImpl {

  private String mimeType;

  public ConfiguredContentTypeProvider() {
    setMimeType("text/plain");
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
   * @param contentType the base content type; defaults to text/plain
   */
  public void setMimeType(String contentType) {
    this.mimeType = contentType;
  }

}
