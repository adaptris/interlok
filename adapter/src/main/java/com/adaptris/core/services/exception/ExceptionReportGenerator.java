package com.adaptris.core.services.exception;

import org.w3c.dom.Document;

/**
 * Interface for generating an XML report from an exception for use with {@link ExceptionReportService}
 * 
 * @author lchan
 */
public interface ExceptionReportGenerator {

  /**
   * Create a Document from the exception.
   *
   * @param e the exception
   * @return a document ready to be merged.
   * @throws Exception on error.
   */
  Document create(Exception e) throws Exception;
}
