package com.adaptris.core.http;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Returns a null as the content-type.
 * 
 * @config http-null-content-type-provider
 */
@XStreamAlias("http-null-content-type-provider")
public class NullContentTypeProvider implements ContentTypeProvider {

  public NullContentTypeProvider() {
  }

  @Override
  public String getContentType(AdaptrisMessage msg) {
    return null;
  }

}
