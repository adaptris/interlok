package com.adaptris.core.transform;

import org.w3c.dom.Document;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Used with {@link XmlValidationService} to validate that a message is in fact XML.
 * 
 * @config xml-basic-validator
 * @license BASIC
 * @author lchan
 * 
 */
@XStreamAlias("xml-basic-validator")
public class XmlBasicValidator extends MessageValidatorImpl {

  @Override
  public void validate(AdaptrisMessage msg) throws CoreException {
    try {
      Document d = XmlHelper.createDocument(msg, null);
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
  }
}
