/*
 * $Author: lchan $
 * $RCSfile: Mail.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/09/21 13:49:33 $
 */
package com.adaptris.mail;

/** Mail Constants.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface Mail {

  /** The Content Encoding header */
  String CONTENT_ENCODING = "Content-Transfer-Encoding";
  /** The Content type header */
  String CONTENT_TYPE = "Content-Type";
  /** The Content dispostition Header */
  String CONTENT_DISPOSITION = "Content-disposition";
  /** The type of disposition */
  String DISPOSITION_TYPE_ATTACHMENT = "attachment";
  /** A parameter to content disposition */
  String DISPOSITION_PARAM_FILENAME = "filename";
}