package com.adaptris.core.lms;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.io.File;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Message factory that creates file backed messages from ZIP files. The first entry in the zip 
 * file will become the contents of the {@link AdaptrisMessage}. Any other entries in the file 
 * will be ignored. This is useful for processing large zip files without having to separately 
 * extract them before sending them in to the adapter.
 */
@XStreamAlias("zip-file-backed-message-factory")
public class ZipFileBackedMessageFactory extends FileBackedMessageFactory {

  public enum CompressionMode {
    /**
     * Compress mode will expect uncompressed data as message payload and write it to a 
     * zip file. Reading from the message in this mode will yield compressed data. 
     */
    Compress, 
    
    /**
     * Uncompress mode will expect compressed data as the message payload and yield
     * uncompressed data when the message is being read.
     */
    Uncompress, 
    
    /**
     * Both mode expects uncompressed data as message payload and yield uncompressed
     * data when reading the message back. The data is only compressed in the temporary
     * file.
     */
    Both
  }
  
  private CompressionMode compressionMode;
  
  public ZipFileBackedMessageFactory() {
    setCompressionMode(CompressionMode.Uncompress);
  }
  
  @Override
  public AdaptrisMessage newMessage() {
    AdaptrisMessage m = new ZipFileBackedMessageImpl(uniqueIdGenerator, this, 
        new File(getTempDirectory()), getDefaultBufferSize(), getMaxMemorySizeBytes(), getCompressionMode());
    
    if (!isEmpty(getDefaultCharEncoding())) {
      m.setCharEncoding(getDefaultCharEncoding());
    }
    
    return m;
  }

  public CompressionMode getCompressionMode() {
    return compressionMode;
  }

  /**
   * The compression mode of the ZipFileBackedMessage. Choose a mode depending on what you want to do:
   * <ul>
   *   <li>Compress - For creating zip files</li>
   *   <li>Uncompress - For reading from zip files</li>
   *   <li>Both - For saving temporary file space</li>
   * </ul>
   */
  public void setCompressionMode(CompressionMode compressionMode) {
    this.compressionMode = compressionMode;
  }
  
}
