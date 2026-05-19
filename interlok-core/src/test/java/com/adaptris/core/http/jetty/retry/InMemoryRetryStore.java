package com.adaptris.core.http.jetty.retry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;

import static com.adaptris.core.http.jetty.retry.FilesystemRetryStore.NAME_ERROR_LINE_SEPERATOR;

// While it's perfectly reasonable to "mock" an in memory one isn't an awful choice for testing.
// However, it's of *no use in real life*.
public class InMemoryRetryStore implements RetryStore {

  private static final transient Map<String, AdaptrisMessage> STORE =
      Collections.synchronizedMap(new HashMap<>());

  @Override
  public void write(AdaptrisMessage msg) throws InterlokException {
    STORE.put(msg.getUniqueId(), msg);
  }

  @Override
  public AdaptrisMessage buildForRetry(String msgId, Map<String, String> metadata,
      AdaptrisMessageFactory factory) throws InterlokException {
    if (STORE.containsKey(msgId)) {
      return STORE.get(msgId);
    }
    throw new InterlokException(msgId + " not found");
  }

  @Override
  public Map<String, String> getMetadata(String msgId) throws InterlokException {
    if (STORE.containsKey(msgId)) {
      return new HashMap<>(STORE.get(msgId).getMessageHeaders());
    }
    throw new InterlokException(msgId + " not found");
  }

  @Override
  public boolean delete(String msgId) throws InterlokException {
    return STORE.remove(msgId) != null;
  }

  @Override
  public Iterable<RemoteBlob> report(boolean includeErrorMessage) throws InterlokException {
    return STORE.entrySet().stream()
      .map((e) -> {
        String name = e.getKey();
        if (includeErrorMessage) {
          String stacktraceFirstLine = null;
          try {
            stacktraceFirstLine = getStacktraceFirstLine(e.getKey());
          } catch (InterlokException ignored) {
          }
            name += NAME_ERROR_LINE_SEPERATOR + stacktraceFirstLine;
        }
        return new RemoteBlob.Builder()
          .setBucket("bucket")
          .setLastModified(System.currentTimeMillis())
          .setName(name)
          .setSize(e.getValue()
          .getSize())
          .build();
      })
      .collect(Collectors.toList());
  }

  public static void removeAll() {
    STORE.clear();
  }

  @Override
  public void acknowledge(String acknowledgeId) throws InterlokException {
   // null implementation
  }

  @Override
  public void deleteAcknowledged() throws InterlokException {
   // null implementation   
  }

  @Override
  public void updateRetryCount(String messageId) throws InterlokException {
   // null implementation   
  }

  @Override
  public void makeConnection(AdaptrisConnection connection) {
   // null implementation 
  }

    @Override
    public String getStackTrace(String msgId) throws InterlokException {
        if (STORE.containsKey(msgId)) {
            AdaptrisMessage message = STORE.get(msgId);
            return message.getContent();
        }
        throw new InterlokException("Stack trace not found for message ID: " + msgId);
    }
}
