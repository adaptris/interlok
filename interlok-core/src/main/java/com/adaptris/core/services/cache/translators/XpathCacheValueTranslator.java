package com.adaptris.core.services.cache.translators;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.cache.CacheKeyTranslator;
import com.adaptris.core.services.cache.CacheValueTranslator;
import com.adaptris.core.util.DocumentBuilderFactoryBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.adaptris.util.text.xml.XPath;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Implementation of {@link CacheValueTranslator} that retrieves a value from the supplied {@link AdaptrisMessage} using an XPath.
 * <p>
 * <strong>Note: this class only supports retrieval of data, not insertion</strong>
 * </p>
 * 
 * @config xpath-cache-value-translator
 * 
 * 
 * @author stuellidge
 */
@XStreamAlias("xpath-cache-value-translator")
public class XpathCacheValueTranslator implements CacheValueTranslator<String>, CacheKeyTranslator {

  @NotBlank
  @InputFieldHint(expression = true)
  private String xpath;
  @Valid
  private KeyValuePairSet namespaceContext = null;
  @AdvancedConfig
  @Valid
  private DocumentBuilderFactoryBuilder xmlDocumentFactoryConfig = null;
  public XpathCacheValueTranslator() {

  }

  public XpathCacheValueTranslator(String xpath) {
    this();
    setXpath(xpath);
  }

  /**
   * @return the result of applying the configured xpath against the payload of this {@link AdaptrisMessage}
   */
  @Override
  public String getValueFromMessage(AdaptrisMessage msg) throws CoreException {
    NamespaceContext ctx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    DocumentBuilderFactoryBuilder builder = documentFactoryBuilder();
    String result = null;
    try {
      XPath xp = XPath.newXPathInstance(builder, ctx);
      Document d = XmlHelper.createDocument(msg, builder);
      result = xp.selectSingleTextItem(d, msg.resolve(getXpath()));
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
    return result;
  }

  /**
   * Sets the XPath to use to query the message
   *
   * @param s
   */
  public void setXpath(String s) {
    xpath = s;
  }

  public String getXpath() {
    return xpath;
  }

  /**
   * Specify a NamespaceContext to use when performing XPath queries
   *
   * @param namespaceContext
   */
  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }

  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  public DocumentBuilderFactoryBuilder getXmlDocumentFactoryConfig() {
    return xmlDocumentFactoryConfig;
  }

  public void setXmlDocumentFactoryConfig(DocumentBuilderFactoryBuilder xml) {
    this.xmlDocumentFactoryConfig = xml;
  }

  DocumentBuilderFactoryBuilder documentFactoryBuilder() {
    return getXmlDocumentFactoryConfig() != null ? getXmlDocumentFactoryConfig() : DocumentBuilderFactoryBuilder.newInstance();
  }

  @Override
  public String getKeyFromMessage(AdaptrisMessage msg) throws CoreException {
    return getValueFromMessage(msg);
  }
}
