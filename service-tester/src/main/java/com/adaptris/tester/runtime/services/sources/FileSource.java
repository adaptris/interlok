package com.adaptris.tester.runtime.services.sources;

import com.adaptris.core.fs.FsHelper;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.net.URL;

@XStreamAlias("file-source")
public class FileSource implements Source {

  private transient FsWorker fsWorker = new NioWorker();

  private String file;

  public FileSource(){

  }

  public FileSource(String file){
    setFile(file);
  }

  @Override
  public String getSource() throws SourceException {
    try {
      URL url = FsHelper.createUrlFromString(file, true);
      File fileToRead = FsHelper.createFileReference(url);
      final byte[] fileContents = fsWorker.get(fileToRead);
      return new String(fileContents);
    } catch (Exception e) {
      throw new SourceException("Failed to read file", e);
    }
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getFile() {
    return file;
  }
}
