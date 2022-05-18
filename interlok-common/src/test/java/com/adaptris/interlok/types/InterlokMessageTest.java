package com.adaptris.interlok.types;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;

public class InterlokMessageTest  {

  @Test
  public void testDefaultResolve() {
    MinimalMessageImpl msg = new MinimalMessageImpl();
    assertEquals("%message{metadata}", msg.resolve("%message{metadata}"));
  }

  @Test
  public void testDefaultWrap() throws Exception {
    MinimalMessageImpl msg = new MinimalMessageImpl();
    assertEquals(MinimalMessageImpl.class, msg.wrap((m) -> (MinimalMessageImpl) m).getClass());
  }

  @Test
  public void testDefaultAddMessageHeaders() {
    MinimalMessageImpl msg = new MinimalMessageImpl();
    assertEquals(0, msg.getMessageHeaders().size());
    Map<String, String> metadata = Map.ofEntries(new SimpleEntry<>("one", "1"), new SimpleEntry<>("two", "2"));
    msg.addMessageHeaders(metadata);
    assertEquals(2, msg.getMessageHeaders().size());
  }

  @Test
  public void testDefaultReplaceAllMessageHeaders() {
    MinimalMessageImpl msg = new MinimalMessageImpl();
    assertEquals(0, msg.getMessageHeaders().size());
    Map<String, String> metadata = Map.ofEntries(new SimpleEntry<>("one", "1"), new SimpleEntry<>("two", "2"));
    msg.addMessageHeaders(metadata);
    assertEquals(2, msg.getMessageHeaders().size());
    Map<String, String> newMetadata = Map.ofEntries(new SimpleEntry<>("three", "3"), new SimpleEntry<>("four", "4"));
    msg.replaceAllMessageHeaders(newMetadata);
    assertEquals(2, msg.getMessageHeaders().size());
  }


  // Minimal message implementation.
  @NoArgsConstructor
  private static class MinimalMessageImpl implements InterlokMessage {
    @Getter
    @Setter
    private String uniqueId = UUID.randomUUID().toString();
    @Getter
    private String content;
    @Getter
    @Setter
    private String contentEncoding;
    @Getter
    private Map<String,String> messageHeaders = new HashMap<>();
    @Getter
    private Map<Object, Object> objectHeaders = new HashMap<>();

    @Override
    public void setContent(String payload, String encoding) {
      content = payload;
      setContentEncoding(encoding);
    }

    @Override
    public void setMessageHeaders(Map<String, String> metadata) {
      addMessageHeaders(metadata);
    }

    @Override
    public void clearMessageHeaders() {
      messageHeaders.clear();
    }

    @Override
    public void addMessageHeader(String key, String value) {
      messageHeaders.put(key, value);
    }

    @Override
    public void removeMessageHeader(String key) {
      messageHeaders.remove(key);
    }

    @Override
    public Reader getReader() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Writer getWriter() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Writer getWriter(String encoding) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void addObjectHeader(Object key, Object object) {
      objectHeaders.put(key, object);
    }

    @Override
    public boolean headersContainsKey(String key) {
      return messageHeaders.containsKey(key);
    }

    @Override
    public String resolve(String s, boolean multiline) {
      return s;
    }

    @Override
    public Object resolveObject(String s) {
      throw new UnsupportedOperationException();
    }
  }
}
