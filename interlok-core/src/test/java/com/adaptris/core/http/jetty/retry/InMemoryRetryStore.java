package com.adaptris.core.http.jetty.retry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.interlok.InterlokException;
import com.adaptris.interlok.cloud.RemoteBlob;

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
  public Iterable<RemoteBlob> report() throws InterlokException {
    return STORE.entrySet().stream()
        .map((e) -> new RemoteBlob.Builder().setBucket("bucket")
            .setLastModified(System.currentTimeMillis()).setName(e.getKey())
            .setSize(e.getValue().getSize()).build())
        .collect(Collectors.toList());
  }

  public static void removeAll() {
    STORE.clear();
  }

}
