package com.adaptris.core.util;

import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.xml.sax.EntityResolver;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Allows simple configuration of a {@link DocumentBuilderFactory}.
 * 
 * @config xml-document-builder-configuration
 * @author lchan
 *
 */
@XStreamAlias("xml-document-builder-configuration")
@DisplayOrder(
order = {"validating", "namespaceAware", "xincludeAware", "expandEntityReferences", "coalescing", "ignoreComments",
    "ignoreWhitespace", "features"})
public class DocumentBuilderFactoryBuilder {

  public static final String DISABLE_DOCTYP =
      "http://apache.org/xml/features/disallow-doctype-decl";

  @NotNull
  @AutoPopulated
  @AdvancedConfig
  private KeyValuePairSet features;
  @AdvancedConfig
  private Boolean validating;
  @AdvancedConfig
  private Boolean namespaceAware;
  @AdvancedConfig
  private Boolean ignoreWhitespace;
  @AdvancedConfig
  private Boolean expandEntityReferences;
  @AdvancedConfig
  private Boolean ignoreComments;
  @AdvancedConfig
  private Boolean coalescing;
  @AdvancedConfig
  private Boolean xincludeAware;

  @AdvancedConfig
  @Valid
  private EntityResolver entityResolver;

  private static enum FactoryConfiguration {
    Validating() {
      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getValidating() != null) {
          f.setValidating(b.getValidating());
        }
      }

    },
    NamespaceAware() {
      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getNamespaceAware() != null) {
          f.setNamespaceAware(b.getNamespaceAware());
        }
      }
    },
    IgnoreWhitespace() {

      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getIgnoreWhitespace() != null) {
          f.setIgnoringElementContentWhitespace(b.getIgnoreWhitespace());
        }
      }

    },
    ExpandEntityRef() {

      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getExpandEntityReferences() != null) {
          f.setExpandEntityReferences(b.getExpandEntityReferences());
        }
      }

    },
    IgnoreComments() {

      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getIgnoreComments() != null) {
          f.setIgnoringComments(b.getIgnoreComments());
        }
      }

    },
    Coalescing() {

      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getCoalescing() != null) {
          f.setCoalescing(b.getCoalescing());
        }
      }

    },
    XInclude() {

      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        if (b.getXincludeAware() != null) {
          f.setXIncludeAware(b.getXincludeAware());
        }
      }
    },
    Features() {

      @Override
      void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException {
        for (KeyValuePair entry : b.getFeatures()) {
          f.setFeature(entry.getKey(), BooleanUtils.toBoolean(entry.getValue()));
        }
      }
    };
    abstract void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException;
  }

  public DocumentBuilderFactoryBuilder() {
    features = new KeyValuePairSet();
  }

  /**
   * Create a new instance that is namespace aware.
   * 
   * @return a new instance.
   */
  public static final DocumentBuilderFactoryBuilder newInstance() {
    return new DocumentBuilderFactoryBuilder().withNamespaceAware(true);
  }

  /**
   * Create a New instance that disables Entityrefs and also mitigates against XXE via
   * {@code http://apache.org/xml/features/disallow-doctype-decl = true}. This is added as a convenience so you don't have to keep
   * configuring it if XXE is a bit of a bother for you.
   * 
   * @return a new instance.
   */
  public static final DocumentBuilderFactoryBuilder newRestrictedInstance() {
    return new DocumentBuilderFactoryBuilder().withNamespaceAware(true).withExpandEntityReferences(false)
        .addFeature(DISABLE_DOCTYP, Boolean.TRUE);
  }

  public static final DocumentBuilderFactoryBuilder newInstanceIfNull(DocumentBuilderFactoryBuilder b) {
    return ObjectUtils.defaultIfNull(b, newInstance());
  }

  public static final DocumentBuilderFactoryBuilder newRestrictedInstanceIfNull(DocumentBuilderFactoryBuilder b) {
    return ObjectUtils.defaultIfNull(b, newRestrictedInstance());
  }

  public static final DocumentBuilderFactoryBuilder newInstanceIfNull(DocumentBuilderFactoryBuilder b, NamespaceContext ctx) {
    if (b != null) {
      return b;
    }
    return ctx == null ? newInstance() : newInstance().withNamespaceAware(true);
  }


  /**
   * Configure a document builder factory
   * 
   * @param f
   * @return a reconfigured document builder factory
   */
  public DocumentBuilderFactory configure(DocumentBuilderFactory f) throws ParserConfigurationException {
    for (FactoryConfiguration c : FactoryConfiguration.values()) {
      c.applyConfig(this, f);
    }
    return f;
  }

  /**
   * Configure a document builder.
   * 
   * @param db
   * @return a reconfigured document builder
   */
  public DocumentBuilder configure(DocumentBuilder db) {
    if (getEntityResolver() != null) {
      db.setEntityResolver(getEntityResolver());
    }
    return db;
  }

  /**
   * Convenience to create a new {@code DocumentBuilder} instance.
   * 
   * @param f a DocumentBuilderFactory
   * @return a configured DocumentBuilder
   * @throws ParserConfigurationException
   * @see #configure(DocumentBuilder)
   * @see #configure(DocumentBuilderFactory)
   */
  public DocumentBuilder newDocumentBuilder(DocumentBuilderFactory f) throws ParserConfigurationException {
    return configure(configure(f).newDocumentBuilder());
  }

  /**
   * Create a {@link DocumentBuilderFactory}.
   * 
   * <p>
   * If all you're doing is creating a {@link DocumentBuilder} straight after calling this method,
   * don't forget to call {@link #configure(DocumentBuilder)} to make sure you configure the
   * underlying {@link DocumentBuilder} with any configured {@link #getEntityResolver()}.
   * </p>
   */
  public DocumentBuilderFactory build() throws ParserConfigurationException {
    return configure(DocumentBuilderFactory.newInstance());
  }

  public KeyValuePairSet getFeatures() {
    return features;
  }


  /**
   * Set the features.
   * 
   * @param features the features.
   */
  public void setFeatures(KeyValuePairSet features) {
    this.features = Args.notNull(features, "Features");
  }


  public DocumentBuilderFactoryBuilder withFeatures(Map<String, Boolean> f) {
    Map<String, Boolean> featureList = Args.notNull(f, "features");
    KeyValuePairSet newFeatures = new KeyValuePairSet();
    for (Map.Entry<String, Boolean> entry : featureList.entrySet()) {
      newFeatures.add(new KeyValuePair(entry.getKey(), String.valueOf(entry.getValue())));
    }
    setFeatures(newFeatures);
    return this;
  }

  public DocumentBuilderFactoryBuilder withFeatures(KeyValuePairSet v) {
    setFeatures(v);
    return this;
  }

  public DocumentBuilderFactoryBuilder addFeature(String featureName, Boolean value) {
    getFeatures().add(new KeyValuePair(featureName, value.toString()));
    return this;
  }

  public Boolean getValidating() {
    return validating;
  }

  /**
   * Maps to {@link DocumentBuilderFactory#setValidating(boolean)}.
   */
  public void setValidating(Boolean validate) {
    validating = validate;
  }

  public DocumentBuilderFactoryBuilder withValidating(Boolean b) {
    setValidating(b);
    return this;
  }

  public Boolean getNamespaceAware() {
    return namespaceAware;
  }


  /**
   * Maps to {@link DocumentBuilderFactory#setNamespaceAware(boolean)}.
   */
  public void setNamespaceAware(Boolean namespaceAware) {
    this.namespaceAware = namespaceAware;
  }

  public DocumentBuilderFactoryBuilder withNamespaceAware(Boolean b) {
    setNamespaceAware(b);
    return this;
  }

  public DocumentBuilderFactoryBuilder withNamespaceAware(NamespaceContext b) {
    setNamespaceAware(b != null ? true : false);
    return this;
  }

  public Boolean getIgnoreWhitespace() {
    return ignoreWhitespace;
  }


  /**
   * Maps to {@link DocumentBuilderFactory#setIgnoringElementContentWhitespace(boolean)}.
   */
  public void setIgnoreWhitespace(Boolean ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }

  public DocumentBuilderFactoryBuilder withIgnoreWhitespace(Boolean b) {
    setIgnoreWhitespace(b);
    return this;
  }


  public Boolean getExpandEntityReferences() {
    return expandEntityReferences;
  }


  /**
   * Maps to {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
   */
  public void setExpandEntityReferences(Boolean expandEntityReferences) {
    this.expandEntityReferences = expandEntityReferences;
  }

  public DocumentBuilderFactoryBuilder withExpandEntityReferences(Boolean b) {
    setExpandEntityReferences(b);
    return this;
  }

  public Boolean getIgnoreComments() {
    return ignoreComments;
  }


  /**
   * Maps to {@link DocumentBuilderFactory#setIgnoringComments(boolean)}.
   */
  public void setIgnoreComments(Boolean ignoreComments) {
    this.ignoreComments = ignoreComments;
  }

  public DocumentBuilderFactoryBuilder withIgnoreComments(Boolean b) {
    setIgnoreComments(b);
    return this;
  }

  public Boolean getCoalescing() {
    return coalescing;
  }


  /**
   * Maps to {@link DocumentBuilderFactory#setCoalescing(boolean)}.
   */
  public void setCoalescing(Boolean coalescing) {
    this.coalescing = coalescing;
  }

  public DocumentBuilderFactoryBuilder withCoalescing(Boolean b) {
    setCoalescing(b);
    return this;
  }


  public Boolean getXincludeAware() {
    return xincludeAware;
  }


  /**
   * Maps to {@link DocumentBuilderFactory#setXIncludeAware(boolean)}.
   */
  public void setXincludeAware(Boolean xincludeAware) {
    this.xincludeAware = xincludeAware;
  }

  public DocumentBuilderFactoryBuilder withXIncludeAware(Boolean b) {
    setXincludeAware(b);
    return this;
  }

  public EntityResolver getEntityResolver() {
    return entityResolver;
  }

  public void setEntityResolver(EntityResolver e) {
    entityResolver = e;
  }

  public DocumentBuilderFactoryBuilder withEntityResolver(EntityResolver e) {
    setEntityResolver(e);
    return this;
  }
}
