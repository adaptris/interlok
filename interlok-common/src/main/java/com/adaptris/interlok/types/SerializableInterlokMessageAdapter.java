package com.adaptris.interlok.types;

import lombok.NonNull;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A wrapper that adapts InterlokMessage to SerializableMessage and retains access to
 * the underlying InterlokMessage
 */
public class SerializableInterlokMessageAdapter implements SerializableMessage {
    private InterlokMessage message;
    private String nextServiceId;
    public SerializableInterlokMessageAdapter(@NonNull InterlokMessage m) {
        this.message = m;
    }

    public InterlokMessage getMessage() {
        return message;
    }

    @Override
    public String getUniqueId() {
        return message.getUniqueId();
    }

    @Override
    public void setUniqueId(String uniqueId) {
        message.setUniqueId(uniqueId);
    }

    @Override
    public String getContent() {
        return message.getContent();
    }

    @Override
    public void setContent(String payload) {
        message.setContent(payload, getContentEncoding() != null ? getContentEncoding() : Charset.defaultCharset().name());
    }

    @Override
    public Map<String, String> getMessageHeaders() {
        return message.getMessageHeaders();
    }

    @Override
    public void setMessageHeaders(Map<String, String> metadata) {
        message.setMessageHeaders(metadata);
    }

    @Override
    public void addMessageHeader(String key, String value) {
        message.addMessageHeader(key, value);
    }

    @Override
    public void removeMessageHeader(String key) {
        message.removeMessageHeader(key);
    }

    @Override
    public String getContentEncoding() {
        return message.getContentEncoding();
    }

    @Override
    public void setContentEncoding(String payloadEncoding) {
        message.setContentEncoding(payloadEncoding);
    }

    @Override
    public void setNextServiceId(String next) {
        this.nextServiceId = next;
    }

    @Override
    public String getNextServiceId() {
        return this.nextServiceId;
    }
}
