package com.adaptris.tester.runtime.messages.payload;

import com.adaptris.core.fs.FsHelper;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.File;
import java.net.URL;

@XStreamAlias("file-payload-provider")
public class FilePayloadProvider implements PayloadProvider {

  private transient FsWorker fsWorker = new NioWorker();

  private String file;

  public void setFile(String file) {
    this.file = file;
  }

  public String getFile() {
    return file;
  }

  @Override
  public String getPayload(){
    try {
      URL url = FsHelper.createUrlFromString(file, true);
      File fileToRead = FsHelper.createFileReference(url);
      final byte[] fileContents = fsWorker.get(fileToRead);
      return new String(fileContents);
    } catch (Exception e) {
      return "";
    }
  }
}
