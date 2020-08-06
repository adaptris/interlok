package com.adaptris.core;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK;
import static com.adaptris.core.CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerCallbackHelper {
  
  protected static final Logger log = LoggerFactory.getLogger(ListenerCallbackHelper.class.getName());
  
  public static AdaptrisMessage prepare(AdaptrisMessage msg, Consumer<AdaptrisMessage> onSuccess) {
    return prepare(msg, onSuccess, null);
  }
  
  public static AdaptrisMessage prepare(AdaptrisMessage msg, Consumer<AdaptrisMessage> onSuccess, Consumer<AdaptrisMessage> onFailure) {
    // since msg.clone() copies object metadata, we're good doing this, since the consumer
    // object should be preserved across clones
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, onSuccess);
    if(onFailure != null)
      msg.addObjectHeader(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, onFailure);
    return msg;
  }

  public static AdaptrisMessage handleSuccessCallback(AdaptrisMessage msg) {
    log.trace("Handling success callback for message [{}]", msg.getUniqueId());
    Consumer c = defaultIfNull((Consumer) msg.getObjectHeaders().get(OBJ_METADATA_ON_SUCCESS_CALLBACK), (o) -> {   });
    c.accept(msg);
    return msg;
  }
  
  public static AdaptrisMessage handleFailureCallback(AdaptrisMessage msg) {
    log.trace("Handling failure callback for message [{}]", msg.getUniqueId());
    Consumer c = defaultIfNull((Consumer) msg.getObjectHeaders().get(OBJ_METADATA_ON_FAILURE_CALLBACK), (o) -> {   });
    c.accept(msg);
    return msg;
  }
}
