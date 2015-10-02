package com.adaptris.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.interlok.config.DataDestination;
import com.adaptris.interlok.types.InterlokMessage;
import com.adaptris.util.URLString;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("file-data-destination")
public class FileDataDestination implements DataDestination<String> {

  @NotBlank
  private String url;

  public FileDataDestination() {

  }

  @Override
  public String getData(InterlokMessage message) throws CoreException {
    try {
      return this.load(new URLString(this.getUrl()), message.getContentEncoding());
    } catch (IOException ex) {
      throw new CoreException(ex);
    }
  }

  @Override
  public void setData(InterlokMessage message, String data) throws CoreException {
    OutputStream outputStream = null;
    try {
      outputStream = this.connectToOutputUrl(new URLString(this.getUrl()));
      IOUtils.write((String) data, outputStream, message.getContentEncoding());
    } catch (IOException e) {
      throw new CoreException(e);
    } finally {
      IOUtils.closeQuietly(outputStream);
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

  private OutputStream connectToOutputUrl(URLString loc) throws IOException {
    if (loc.getProtocol() == null || "file".equals(loc.getProtocol())) {
      return connectToOutputFile(loc.getFile());
    }
    URL url = new URL(loc.toString());
    URLConnection conn = url.openConnection();
    return conn.getOutputStream();
  }

  private OutputStream connectToOutputFile(String localFile) throws IOException {
    OutputStream out = null;
    File f = new File(localFile);
    if (f.exists()) {
      out = new FileOutputStream(f);
    } else {
      ClassLoader c = this.getClass().getClassLoader();
      URL u = c.getResource(localFile);
      if (u != null) {
        out = new FileOutputStream(new File(u.getPath()));
      }
    }
    return out;
  }

}
