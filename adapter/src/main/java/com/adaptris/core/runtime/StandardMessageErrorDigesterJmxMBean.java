package com.adaptris.core.runtime;

public interface StandardMessageErrorDigesterJmxMBean extends ChildRuntimeInfoComponentMBean {

  MessageErrorDigest getDigest();

  MessageErrorDigest getDigestSubset(int fromIndex);

  MessageErrorDigest getDigestSubset(int fromIndex, int toIndex);

  int getTotalErrorCount();

  boolean remove(MessageDigestErrorEntry entry);

  boolean remove(String uniqueId);
}
