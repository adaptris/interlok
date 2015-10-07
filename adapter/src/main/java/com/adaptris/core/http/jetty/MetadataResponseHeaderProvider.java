package com.adaptris.core.http.jetty;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.server.ResponseHeaderProvider;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link ResponseHeaderProvider} implementation that providers HTTP response headers from metadata.
 * 
 * @config jetty-metadata-response-headers
 * 
 */
@XStreamAlias("jetty-metadata-response-headers")
public class MetadataResponseHeaderProvider implements ResponseHeaderProvider<HttpServletResponse> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  @NotNull
  @Valid
  private MetadataFilter filter;

  public MetadataResponseHeaderProvider() {

  }

  public MetadataResponseHeaderProvider(MetadataFilter f) {
    this();
    setFilter(f);
  }

  @Override
  public HttpServletResponse addHeaders(AdaptrisMessage msg, HttpServletResponse target) {
    MetadataCollection subset = getFilter().filter(msg);
    for (MetadataElement me : subset) {
      log.trace("Adding Response Header [{}: {}]", me.getKey(), me.getValue());
      target.addHeader(me.getKey(), me.getValue());
    }
    return target;
  }

  public MetadataFilter getFilter() {
    return filter;
  }

  public void setFilter(MetadataFilter filter) {
    this.filter = Args.notNull(filter, "Metadata Filter");
  }

}
