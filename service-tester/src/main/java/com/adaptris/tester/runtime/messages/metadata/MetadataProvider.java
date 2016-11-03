package com.adaptris.tester.runtime.messages.metadata;

import java.util.Map;

public interface MetadataProvider {

   Map<String, String> getMessageHeaders();
}
