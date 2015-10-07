package com.adaptris.core.common;

import java.io.IOException;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@code DataInputParameter} implementation that reads from a file.
 * 
 * @config file-data-input-parameter
 *
 */
@XStreamAlias("file-data-input-parameter")
public class FileDataInputParameter extends FileInputParameterImpl {

  @NotBlank
  private String url;

  public FileDataInputParameter() {

  }

  @Override
  public String extract(InterlokMessage message) throws CoreException {
    try {
      return this.load(new URLString(this.getUrl()), message.getContentEncoding());
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }


}
