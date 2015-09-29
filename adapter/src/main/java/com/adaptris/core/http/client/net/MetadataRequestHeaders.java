package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link RequestHeaderHandler} that applies {@link AdaptrisMessage} metadata as
 * headers to a {@link HttpURLConnection}.
 * 
 * @config http-metadata-request-headers
 * 
 */
@XStreamAlias("http-metadata-request-headers")
public class MetadataRequestHeaders implements RequestHeaderProvider<HttpURLConnection> {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  @NotNull
  @Valid
  private MetadataFilter filter;

  public MetadataRequestHeaders() {
  }

  public MetadataRequestHeaders(MetadataFilter mf) {
    this();
    setFilter(mf);
  }

  @Override
  public HttpURLConnection addHeaders(AdaptrisMessage msg, HttpURLConnection target) {
    Map<String, String> result = new HashMap<>();
    MetadataCollection metadataSubset = getFilter().filter(msg);
    for (MetadataElement me : metadataSubset) {
      log.trace("Adding Request Property [{}:{}]", me.getKey(), me.getValue());
      target.addRequestProperty(me.getKey(), me.getValue());
    }
    return target;
  }

  public MetadataFilter getFilter() {
    return filter;
  }

  /**
   * Set the filter to be applied to metadata before adding as request properties.
   * 
   * @param mf the filter.
   */
  public void setFilter(MetadataFilter mf) {
    this.filter = Args.notNull(mf, "metadata filter");
  }

}
