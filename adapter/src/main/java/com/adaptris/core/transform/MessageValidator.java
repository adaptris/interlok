package com.adaptris.core.transform;

import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;

public interface MessageValidator extends AdaptrisComponent {

  void validate(AdaptrisMessage msg) throws CoreException;

}
