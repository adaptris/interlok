package com.adaptris.core.http;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.Locale;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides a content type derived from metadata.
 * <p>
 * Note that the content type charset will be derived from {@link AdaptrisMessage#getCharEncoding()}
 * so configuring a mime type of {@code text/xml} when the message has a char encoding of
 * {@code UTF-8} will return {@code text/xml; charset="UTF-8"}. No validation is done on the resulting string or on the value
 * that is taken from metadata.
 * </p>
 * 
 * @config http-metadata-content-type-provider
 */
@XStreamAlias("http-metadata-content-type-provider")
public class MetadataContentTypeProvider extends ContentTypeProviderImpl {

  @NotBlank
  private String metadataKey;
  @AutoPopulated
  @NotBlank
  private String defaultMimeType;


  public MetadataContentTypeProvider() {
    setDefaultMimeType("text/plain");
  }

  public MetadataContentTypeProvider(String key) {
    this();
    setMetadataKey(key);
  }

  @Override
  public String getContentType(AdaptrisMessage msg) throws CoreException {
    return build(extract(msg), msg.getCharEncoding());
  }

  private String extract(AdaptrisMessage msg) throws CoreException {
    String result = defaultMimeType;
    if (isBlank(getMetadataKey())) {
      throw new CoreException("metadata key is blank");
    }
    return defaultIfBlank(msg.getMetadataValue(getMetadataKey()), defaultMimeType).toLowerCase(Locale.ROOT);
  }


  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata item containing content type.
   * 
   * @param key the key containing the base content type
   */
  public void setMetadataKey(String key) {
    this.metadataKey = Args.notBlank(key, "Metadata Key");
  }

  public String getDefaultMimeType() {
    return defaultMimeType;
  }

  /**
   * Set the default mime type to use if the metadata key does not exist.
   * 
   * @param mt the mime type; defaults to text/plain
   */
  public void setDefaultMimeType(String mt) {
    this.defaultMimeType = Args.notBlank(mt, "Default Mime Type");
  }

}
