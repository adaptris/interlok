package com.adaptris.core.http.client.net;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.http.client.ResponseHeaderHandler;

public abstract class MetadataResponseHeaderImpl implements ResponseHeaderHandler<HttpURLConnection> {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  private String metadataPrefix;

  public MetadataResponseHeaderImpl() {

  }

  protected String generateKey(String header) {
    return defaultIfEmpty(getMetadataPrefix(), "") + header;
  }

  public String getMetadataPrefix() {
    return metadataPrefix;
  }

  public void setMetadataPrefix(String metadataPrefix) {
    this.metadataPrefix = metadataPrefix;
  }


  @Override
  public AdaptrisMessage handle(HttpURLConnection src, AdaptrisMessage msg) {
    addMetadata(src.getHeaderFields(), msg);
    return msg;
  }

  protected abstract void addMetadata(Map<String, List<String>> headers, AdaptrisMessage reply);
}
