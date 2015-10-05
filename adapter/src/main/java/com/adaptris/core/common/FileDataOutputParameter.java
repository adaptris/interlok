package com.adaptris.core.common;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.config.DataOutputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * {@link DataOutputParameter} implementation that writes to a file.
 * 
 * @config file-data-output-parameter
 */
@XStreamAlias("file-data-output-parameter")
public class FileDataOutputParameter implements DataOutputParameter<String> {

  @NotBlank
  private String url;

  public FileDataOutputParameter() {

  }

  @Override
  public void insert(String data, InterlokMessage message) throws CoreException {
    OutputStream out = null;
    try {
      URL url = FsHelper.createUrlFromString(this.getUrl(), true);
      out = new FileOutputStream(FsHelper.createFileReference(url));
      IOUtils.write((String) data, out, message.getContentEncoding());
    } catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
