package com.adaptris.core;

public interface SerializableMessageTranslator extends AdaptrisMessageTranslator {

  SerializableAdaptrisMessage translate(AdaptrisMessage message) throws CoreException;

  AdaptrisMessage translate(SerializableAdaptrisMessage message) throws CoreException;
  
}
