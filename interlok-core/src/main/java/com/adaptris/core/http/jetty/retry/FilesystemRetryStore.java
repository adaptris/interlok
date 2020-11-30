package com.adaptris.core.http.jetty.retry;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import javax.validation.constraints.NotBlank;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.lang3.BooleanUtils;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.fs.FsHelper;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.MetadataHelper;
import com.adaptris.fs.FsWorker;
import com.adaptris.fs.NioWorker;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.util.Args;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Writes data into local storage for retry.
 * <p>
 * While not tightly coupled with {@link RetryFromJetty} it is designed somewhat exclusively for
 * that. You may be able to make use of it in other ways, but behaviour may change unexpectedly due
 * to changes in {@link RetryFromJetty}.
 * </p>
 * <p>
 * The behaviour of this store will assume that each {@code message-id} will form a sub-directory
 * off {@code baseUrl}. Metadata will be stored {@code [baseUrl]/[msgId]/metadata.properties} as a
 * standard properties file; the payload will be stored in {@code [baseUrl]/[msgId]/payload.blob}.
 * <p>
 *
 * @since 3.11.1
 * @config retry-store-filesystem
 */
@XStreamAlias("retry-store-filesystem")
@ComponentProfile(summary = "Store message for retry on the filesystem.", since = "3.11.1")
@DisplayOrder(order = {"baseUrl"})
@Slf4j
public class FilesystemRetryStore implements RetryStore {

  private static final String PAYLOAD_FILE_NAME = "payload.blob";
  private static final String METADATA_FILE_NAME = "metadata.properties";

  /**
   * The base URL {@code file:///...} where we can discover files.
   *
   */
  @Getter
  @Setter
  @NotBlank
  private String baseUrl;

  private transient NioWorker fsWorker = new NioWorker();

  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getBaseUrl(), "baseUrl");
  }

  @Override
  public void write(AdaptrisMessage msg) throws InterlokException {
    try {
      File dir = validateMsgId(msg.getUniqueId(), false);
      log.trace("Created [{}]", dir.getCanonicalPath());
      File payloadFile = new File(dir, PAYLOAD_FILE_NAME);
      File metadataFile = new File(dir, METADATA_FILE_NAME);
      if (msg instanceof FileBackedMessage) {
        FileUtils.copyFile(((FileBackedMessage) msg).currentSource(), payloadFile);
      } else {
        try (InputStream in = msg.getInputStream();
            OutputStream out = new FileOutputStream(payloadFile)) {
          IOUtils.copy(in, out);
        }
      }
      log.trace("Wrote [{}]", payloadFile.getCanonicalPath());
      try (OutputStream out = new FileOutputStream(metadataFile)) {
        Properties p = MetadataHelper.convertToProperties(msg.getMetadata());
        p.store(out, "Metadata for " + msg.getUniqueId());
      }
      log.trace("Wrote [{}]", metadataFile.getCanonicalPath());
    } catch (Exception e) {
      throw ExceptionHelper.wrapInterlokException(e);
    }
  }

  private File validateMsgId(String msgId, boolean mustAlreadyExist) throws Exception {
    File target = new File(FsHelper.toFile(getBaseUrl()), msgId);
    return validateDir(target, mustAlreadyExist);
  }

  private File validateDir(File target, boolean mustAlreadyExist) throws Exception {
    if (mustAlreadyExist) {
      FsWorker.checkReadable(FsWorker.isDirectory(target));
    } else {
      target.mkdirs();
    }
    return target;
  }

  @Override
  public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata,
      AdaptrisMessageFactory msgFac) throws InterlokException {
    try {
      File dir = validateMsgId(msgId, true);
      File payloadFile = FsWorker.isFile(FsWorker.checkReadable(new File(dir, PAYLOAD_FILE_NAME)));
      AdaptrisMessage msg = DefaultMessageFactory.defaultIfNull(msgFac).newMessage();
      if (msg instanceof FileBackedMessage) {
        ((FileBackedMessage) msg).initialiseFrom(payloadFile);
      } else {
        try (InputStream in = new FileInputStream(payloadFile);
            OutputStream out = msg.getOutputStream()) {
          IOUtils.copy(in, out);
        }
      }
      msg.setMessageHeaders(metadata);
      msg.setUniqueId(msgId);
      return msg;
    } catch (Exception e) {
      throw ExceptionHelper.wrapInterlokException(e);
    }
  }

  @Override
  public Map<String, String> getMetadata(String msgId) throws InterlokException {
    try {
      File dir = validateMsgId(msgId, true);
      File metaFile = FsWorker.isFile(FsWorker.checkReadable(new File(dir, METADATA_FILE_NAME)));
      Properties meta = new Properties();
      try (InputStream in = new FileInputStream(metaFile)) {
        meta.load(in);
      }
      // The compiler works in mysterious ways.
      return (Map) meta;
    } catch (Exception e) {
      throw ExceptionHelper.wrapInterlokException(e);
    }
  }

  @Override
  public Iterable<RemoteBlob> report() throws InterlokException {
    List<RemoteBlob> result = new ArrayList<>();
    try {
      File target = validateDir(FsHelper.toFile(getBaseUrl()), false);
      File[] files = fsWorker.listFiles(target, DirectoryFileFilter.DIRECTORY);
      for (File msgId : files) {
        Optional.ofNullable(createForReport(msgId)).ifPresent((blob) -> result.add(blob));
      }
    } catch (Exception e) {
      throw ExceptionHelper.wrapInterlokException(e);
    }
    return result;
  }

  @Override
  public boolean delete(String msgId) throws InterlokException {
    try {
      File target = new File(FsHelper.toFile(getBaseUrl()), msgId);
      return FileUtils.deleteQuietly(target);
    } catch (Exception e) {
      throw ExceptionHelper.wrapInterlokException(e);
    }
  }

  protected static RemoteBlob createForReport(File baseDir) {
    // assert that both metadata & payload files exist.
    try {
      File payload = new File(baseDir, PAYLOAD_FILE_NAME);
      File metadata = new File(baseDir, METADATA_FILE_NAME);
      if (BooleanUtils.and(new boolean[] {FileUtils.directoryContains(baseDir, payload),
          FileUtils.directoryContains(baseDir, metadata)})) {
        // Return the size of the payload file, but other things like
        // last modified can be derived from the directory.
        return new RemoteBlob.Builder()
            .setLastModified(baseDir.lastModified()).setName(baseDir.getName())
            .setSize(payload.length()).build();
      }
    } catch (Exception e) {
    }
    return null;
  }

  public FilesystemRetryStore withBaseUrl(String s) {
    setBaseUrl(s);
    return this;
  }
}
