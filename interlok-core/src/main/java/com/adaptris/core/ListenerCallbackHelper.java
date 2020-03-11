package com.adaptris.core;

import static com.adaptris.core.CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import java.util.function.Consumer;

public class ListenerCallbackHelper {
  
  public static AdaptrisMessage prepare(AdaptrisMessage msg, Consumer<AdaptrisMessage> onSuccess) {
    // since msg.clone() copies object metadata, we're good doing this, since the consumer
    // object should be preserved across clones
    msg.addObjectHeader(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, onSuccess);
    return msg;
  }


  public static AdaptrisMessage handleSuccessCallback(AdaptrisMessage msg) {
    Consumer c = defaultIfNull((Consumer) msg.getObjectHeaders().get(OBJ_METADATA_ON_SUCCESS_CALLBACK), (o) -> {   });
    c.accept(msg);
    return msg;
  }
}
