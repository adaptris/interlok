package com.adaptris.core.http.client.net;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.http.client.RequestHeaderProvider;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link RequestHeaderHandler} that applies {@link AdaptrisMessage} metadata as
 * headers using a {@link MetadataFilter}.
 * 
 * @config http-metadata-request-headers
 * 
 */
@XStreamAlias("http-metadata-request-headers")
public class MetadataRequestHeaders implements RequestHeaderProvider<HttpURLConnection> {
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
      target.addRequestProperty(me.getKey(), me.getValue());
    }
    return target;
  }

  public MetadataFilter getFilter() {
    return filter;
  }

  public void setFilter(MetadataFilter filter) {
    this.filter = Args.notNull(filter, "metadata filter");
  }

}
