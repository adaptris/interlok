package com.adaptris.core.http;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Version of {@link RequestMethodProvider} that can derive the method from {@link AdaptrisMessage} metadata.
 *
 * <p>If the configured metadata key does not exist, then {@link ConfiguredRequestMethodProvider#getMethod(AdaptrisMessage)} is
 * used to provide the request method.
 * </p>
 * @config http-metadata-request-method
 * @author lchan
 *
 */
@XStreamAlias("http-metadata-request-method")
public class MetadataRequestMethodProvider implements RequestMethodProvider {

  @NotBlank
  private String metadataKey;
  @AutoPopulated
  @NotNull
  private RequestMethod defaultMethod;


  public MetadataRequestMethodProvider() {
    setDefaultMethod(RequestMethod.POST);
  }

  public MetadataRequestMethodProvider(String key) {
    this(key, RequestMethod.POST);
  }

  public MetadataRequestMethodProvider(String key, RequestMethod defMethod) {
    this();
    setMetadataKey(key);
    setDefaultMethod(defMethod);
  }

  @Override
  public RequestMethod getMethod(AdaptrisMessage msg) {
    if (msg.containsKey(getMetadataKey())) {
      return RequestMethod.valueOf(msg.getMetadataValue(getMetadataKey()).toUpperCase());
    }
    return getDefaultMethod();
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = Args.notBlank(metadataKey, "Metadata Key");
  }

  public RequestMethod getDefaultMethod() {
    return defaultMethod;
  }

  /**
   * Set the default method.
   * 
   * @param m the default method ({@link RequestMethod#POST}).
   */
  public void setDefaultMethod(RequestMethod m) {
    this.defaultMethod = Args.notNull(m, "Default HTTP Method");
  }

}
