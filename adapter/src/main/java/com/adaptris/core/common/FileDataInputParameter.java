package com.adaptris.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("file-data-input-parameter")
public class FileDataInputParameter implements DataInputParameter<String> {

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

  private String load(URLString loc, String encoding) throws IOException {
    String content = null;

    try (InputStream inputStream = connectToInputUrl(loc)) {
      StringWriter writer = new StringWriter();
      IOUtils.copy(inputStream, writer, encoding);
      content = writer.toString();
    }
    return content;
  }

  private InputStream connectToInputUrl(URLString loc) throws IOException {
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToInputFile(loc.getFile());
    }
    URL url = new URL(loc.toString());
    URLConnection conn = url.openConnection();
    return conn.getInputStream();
  }

  private InputStream connectToInputFile(String localFile) throws IOException {
    InputStream in = null;
    File f = new File(localFile);
    if (f.exists()) {
      in = new FileInputStream(f);
    } else {
      ClassLoader c = this.getClass().getClassLoader();
      URL u = c.getResource(localFile);
      if (u != null) {
        in = u.openStream();
      }
    }
    return in;
  }
}
