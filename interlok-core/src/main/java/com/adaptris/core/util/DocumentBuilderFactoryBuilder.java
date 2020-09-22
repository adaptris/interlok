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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Allows simple configuration of a {@link DocumentBuilderFactory}.
 *
 * <p>
 * Note that unless explicitly specified then the corresponding {@link DocumentBuilderFactory} will
 * not have its corresponding setter called.
 * </p>
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

  /**
   * Calls {@link DocumentBuilderFactory#setFeature(String, boolean)} for each value defined.
   * <p>
   * No validation of the features is done and are passed as-is through to the underlying
   * DocumentBuilderFactory.
   * </p>
   */
  @NotNull
  @AutoPopulated
  @AdvancedConfig
  @Getter
  @Setter
  @NonNull
  private KeyValuePairSet features;
  /**
   * Calls {@link DocumentBuilderFactory#setValidating(boolean)} if non-null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean validating;
  /**
   * Calls {@link DocumentBuilderFactory#setNamespaceAware(boolean)} if non null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean namespaceAware;
  /**
   * Calls {@link DocumentBuilderFactory#setIgnoringElementContentWhitespace(boolean)} if non-null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean ignoreWhitespace;
  /**
   * Calls {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)} if non-null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean expandEntityReferences;
  /**
   * Calls {@link DocumentBuilderFactory#setIgnoringComments(boolean)} if non-null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean ignoreComments;
  /**
   * Calls {@link DocumentBuilderFactory#setCoalescing(boolean)} if non-null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean coalescing;
  /**
   * Calls {@link DocumentBuilderFactory#setXIncludeAware(boolean)} if non-null
   *
   */
  @AdvancedConfig
  @Getter
  @Setter
  private Boolean xincludeAware;

  /**
   * Calls {@link DocumentBuilder#setEntityResolver(EntityResolver)} if non-null.
   *
   */
  @Getter
  @Setter
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

  public DocumentBuilderFactoryBuilder withFeatures(Map<String, Boolean> f) {
    Map<String, Boolean> featureList = Args.notNull(f, "features");
    KeyValuePairSet newFeatures = new KeyValuePairSet();
    for (Map.Entry<String, Boolean> entry : featureList.entrySet()) {
      newFeatures.add(new KeyValuePair(entry.getKey(), String.valueOf(entry.getValue())));
    }
    return withFeatures(newFeatures);
  }

  public DocumentBuilderFactoryBuilder withFeatures(KeyValuePairSet v) {
    setFeatures(v);
    return this;
  }

  public DocumentBuilderFactoryBuilder addFeature(String featureName, Boolean value) {
    getFeatures().add(new KeyValuePair(featureName, value.toString()));
    return this;
  }

  public DocumentBuilderFactoryBuilder withValidating(Boolean b) {
    setValidating(b);
    return this;
  }


  public DocumentBuilderFactoryBuilder withNamespaceAware(Boolean b) {
    setNamespaceAware(b);
    return this;
  }

  public DocumentBuilderFactoryBuilder withNamespaceAware(NamespaceContext b) {
    setNamespaceAware(b != null ? true : false);
    return this;
  }


  public DocumentBuilderFactoryBuilder withIgnoreWhitespace(Boolean b) {
    setIgnoreWhitespace(b);
    return this;
  }


  public DocumentBuilderFactoryBuilder withExpandEntityReferences(Boolean b) {
    setExpandEntityReferences(b);
    return this;
  }


  public DocumentBuilderFactoryBuilder withIgnoreComments(Boolean b) {
    setIgnoreComments(b);
    return this;
  }

  public DocumentBuilderFactoryBuilder withCoalescing(Boolean b) {
    setCoalescing(b);
    return this;
  }

  public DocumentBuilderFactoryBuilder withXIncludeAware(Boolean b) {
    setXincludeAware(b);
    return this;
  }

  public DocumentBuilderFactoryBuilder withEntityResolver(EntityResolver e) {
    setEntityResolver(e);
    return this;
  }
}
