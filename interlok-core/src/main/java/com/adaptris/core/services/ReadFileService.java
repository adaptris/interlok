package com.adaptris.core.services;

import static com.adaptris.fs.FsWorker.checkReadable;
import static com.adaptris.fs.FsWorker.isFile;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.fs.FsException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Read a file from a specific path into the message payload.
 *
 * @config read-file-service
 */
@AdapterComponent
@ComponentProfile(summary = "Read a file from a specific path into the message payload",
    tag = "service,file")
@XStreamAlias("read-file-service")
public class ReadFileService extends ServiceImp {

  /**
   * The parameter for the path to the file to read.
   */
  @NotBlank
  @InputFieldHint(expression = true)
  private String filePath;

  @AffectsMetadata
  @AdvancedConfig
  @InputFieldDefault(value = "null")
  private String contentTypeMetadataKey;

  @AdvancedConfig(rare = true)
  @Valid
  @InputFieldDefault(value = "Files.probeContentType(Path)")
  private ContentTypeProbe contentTypeProbe;

  @Override
  public void doService(final AdaptrisMessage message) throws ServiceException {
    try {
      final File file = convertToFile(message.resolve(getFilePath()));
      log.trace("Reading file : {}", file.getCanonicalPath());
      try (FileInputStream in = new FileInputStream(file);
          OutputStream out = message.getOutputStream()) {
        copy(in, out);
      }
      if (isNotBlank(getContentTypeMetadataKey())) {
        message.addMetadata(getContentTypeMetadataKey(), probeContentType(file));
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }
  
  private static File convertToFile(String filepath) throws FsException {
    try {
      return isFile(checkReadable(FsHelper.toFile(filepath)));
    } catch (Exception e) {
      return isFile(checkReadable(new File(filepath)));
    }
  }

  private String probeContentType(File file) throws IOException {
    return defaultIfBlank(contentTypeProbe().probeContentType(file), "");
  }

  @Override
  public void prepare() throws CoreException {
    /* empty method */
  }

  @Override
  protected void closeService() {
    /* empty method */
  }

  @Override
  protected void initService() throws CoreException {
    try {
      Args.notBlank(getFilePath(), "filePath");
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  /**
   * Get the file path parameter.
   * 
   * @return The file path parameter.
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * Set the file path parameter.
   * 
   * @param filePath The file path parameter.
   */
  public void setFilePath(final String filePath) {
    this.filePath = Args.notBlank(filePath, "filePath");
  }

  public String getContentTypeMetadataKey() {
    return contentTypeMetadataKey;
  }

  /**
   * Sets the metadata key set the content type as, if not provided will not be set. (default: null)
   * 
   * @param contentTypeMetadataKey
   */
  public void setContentTypeMetadataKey(String contentTypeMetadataKey) {
    this.contentTypeMetadataKey = contentTypeMetadataKey;
  }


  public ContentTypeProbe getContentTypeProbe() {
    return contentTypeProbe;
  }

  /**
   * If {@link #setContentTypeMetadataKey(String)} is set, then this interface is used to probe the content type of the file.
   * <p>
   * By default {@link Files#probeContentType(java.nio.file.Path)} is used which means no additional jars are required. This
   * implementation is platform dependent; so an alternative is available in the {@code com.adaptris:interlok-filesystem} module.
   * </p>
   * 
   * @param contentTypeProbe how to probe for the content type; by default uses {@link Files#probeContentType(java.nio.file.Path)}
   *        if not explicitly configured.
   */
  public void setContentTypeProbe(ContentTypeProbe contentTypeProbe) {
    this.contentTypeProbe = contentTypeProbe;
  }

  protected ContentTypeProbe contentTypeProbe() {
    return getContentTypeProbe() != null ? getContentTypeProbe() : e -> {
      return Files.probeContentType(e.toPath());
    };
  }

  @FunctionalInterface
  public interface ContentTypeProbe {
    String probeContentType(File f) throws IOException;
  }
}
