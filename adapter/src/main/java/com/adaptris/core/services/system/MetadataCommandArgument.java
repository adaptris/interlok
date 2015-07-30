package com.adaptris.core.services.system;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Provides the metadata value associated with the specified key as a command line argument
 * 
 * @config system-command-metadata-argument
 * 
 * @author sellidge
 */
@XStreamAlias("system-command-metadata-argument")
public class MetadataCommandArgument implements CommandArgument {

  @NotBlank
  private String key;
  

  public MetadataCommandArgument() {

  }
  
  public MetadataCommandArgument(String arg) {
    this();
    setKey(arg);
  }
  

  @Override
  public String retrieveValue(AdaptrisMessage msg) {
    return msg.getMetadataValue(key);
  }

  public String getKey() {
    return key;
  }

  /**
   * The metadata key to be dereferenced
   * @param key
   */
  public void setKey(String key) {
    this.key = key;
  }

}
