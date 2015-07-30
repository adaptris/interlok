package com.adaptris.util.text.xml;

import org.w3c.dom.Document;

public interface DocumentMerge {

  /**
   * Merge the contents of two XML documents
   * 
   * @param original the original Document
   * @param newDocument the document to merge
   * @return the merged document
   * @throws Exception
   */
  Document merge(Document original, Document newDocument) throws Exception;
}
