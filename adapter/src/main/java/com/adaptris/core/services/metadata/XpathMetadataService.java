package com.adaptris.core.services.metadata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Document;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisComponent;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.services.metadata.xpath.XpathQuery;
import com.adaptris.core.util.XmlHelper;
import com.adaptris.util.KeyValuePairSet;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.adaptris.util.text.xml.SimpleNamespaceContext;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Service which sets values extracted from message payload using {@link XpathQuery} as metadata.
 * </p>
 * 
 * @config xpath-metadata-service
 * 
 * @license BASIC
 */
@XStreamAlias("xpath-metadata-service")
public class XpathMetadataService extends ServiceImp {

  private KeyValuePairSet namespaceContext;
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit(itemFieldName = "xpath-query")
  private List<XpathQuery> xpathQueries;

  private transient List<XpathQuery> queriesToExecute;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public XpathMetadataService() {
    setXpathQueries(new ArrayList<XpathQuery>());
  }

  /** @see AdaptrisComponent */
  @Override
  public void init() throws CoreException {
    for (XpathQuery query : xpathQueries) {
      query.verify();
    }
    queriesToExecute = new ArrayList<>();
    queriesToExecute.addAll(getXpathQueries());
  }

  public void doService(AdaptrisMessage msg) throws ServiceException {

    Set<MetadataElement> metadataElements = new HashSet<MetadataElement>();
    NamespaceContext namespaceCtx = SimpleNamespaceContext.create(getNamespaceContext(), msg);
    try {
      Document doc = XmlHelper.createDocument(msg, namespaceCtx);
      for (XpathQuery query : queriesToExecute) {
        metadataElements.add(query.resolveXpath(doc, namespaceCtx, query.createXpathQuery(msg)));
      }
      log.debug("Xpath Metadata resolved " + metadataElements);
      msg.setMetadata(metadataElements);
    }
    catch (Exception e) {
      throw new ServiceException(e);
    }
  }


  public KeyValuePairSet getNamespaceContext() {
    return namespaceContext;
  }

  /**
   * Set the namespace context for resolving namespaces.
   * <ul>
   * <li>The key is the namespace prefix</li>
   * <li>The value is the namespace uri</li>
   * </ul>
   *
   * @param namespaceContext
   */
  public void setNamespaceContext(KeyValuePairSet namespaceContext) {
    this.namespaceContext = namespaceContext;
  }

  /** @see AdaptrisComponent */
  @Override
  public void close() { /* na */
  }

  public List<XpathQuery> getXpathQueries() {
    return xpathQueries;
  }

  /**
   * Set the list of {@linkplain XpathQuery} instances that will be executed.
   *
   * @param xql
   */
  public void setXpathQueries(List<XpathQuery> xql) {
    if (xql == null) {
      throw new IllegalArgumentException("Xpath Queries are null");
    }
    xpathQueries = xql;
  }

  public void addXpathQuery(XpathQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("XpathQuery is null");
    }
    xpathQueries.add(query);
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
