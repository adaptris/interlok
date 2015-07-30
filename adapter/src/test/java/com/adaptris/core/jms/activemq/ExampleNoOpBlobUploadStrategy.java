package com.adaptris.core.jms.activemq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.jms.JMSException;

import org.apache.activemq.blob.BlobUploadStrategy;
import org.apache.activemq.command.ActiveMQBlobMessage;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("example-no-op-blob-upload-strategy")
public class ExampleNoOpBlobUploadStrategy implements BlobUploadStrategy {

  public URL uploadFile(ActiveMQBlobMessage arg0, File arg1) throws JMSException, IOException {
    throw new UnsupportedOperationException("Not possible, just an example");
  }

  public URL uploadStream(ActiveMQBlobMessage arg0, InputStream arg1) throws JMSException, IOException {
    throw new UnsupportedOperationException("Not possible, just an example");
  }

}
