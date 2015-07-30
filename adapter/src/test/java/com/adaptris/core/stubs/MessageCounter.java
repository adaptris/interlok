package com.adaptris.core.stubs;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;

public interface MessageCounter {

  List<AdaptrisMessage> getMessages();

  int messageCount();
}
