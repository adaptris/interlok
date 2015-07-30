package com.adaptris.util.text.xml;

import org.w3c.dom.Document;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Merge implementation that simply replaces the original.
 * 
 * @config xml-replace-original
 * 
 * @author lchan
 * 
 */
@XStreamAlias("xml-replace-original")
public class ReplaceOriginal extends MergeImpl {
  public ReplaceOriginal() {

  }

  @Override
  public Document merge(Document original, Document newDoc) throws Exception {
    return newDoc;
  }
}
