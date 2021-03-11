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
import com.adaptris.annotation.InputFieldDefault;
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
 * For security reasons the default behaviour is to mitigate against XXE attacks and the like. It is
 * still possible to explicitly configure whatever behaviour is required, but we no longer rely on
 * the underlying {@code DocumentBuilderFactory} defaults. As a result the vanilla configuration for
 * a {@code DocumentBuilderFactory} that is created by this class will have the following defaults:
 * <ul>
 * <li>expandEntityReferences will be set to false if not otherwise specified</li>
 * <li>The feature {@value #DISABLE_DOCTYP} will be automatically set to true if not otherwise
 * specified</li>
 * </ul>
 * </p>
 * <p>
 * Note that the static convenience methods have also been modified to reflect this behaviour. There
 * are the two new {@link #newLenientInstance()} and
 * {@link #newLenientInstanceIfNull(DocumentBuilderFactoryBuilder)} methods if the more secure
 * default is not appropriate.
 * </p>
 *
 *
 * @config xml-document-builder-configuration
 */
@XStreamAlias("xml-document-builder-configuration")
@DisplayOrder(
order = {"validating", "namespaceAware", "xincludeAware", "expandEntityReferences", "coalescing", "ignoreComments",
    "ignoreWhitespace", "features"})
public class DocumentBuilderFactoryBuilder {


  public static final String DISABLE_DOCTYPE =
      "http://apache.org/xml/features/disallow-doctype-decl";

  /**
   * Calls {@link DocumentBuilderFactory#setFeature(String, boolean)} for each value defined.
   * <p>
   * No validation of the features is done and are passed as-is through to the underlying
   * DocumentBuilderFactory.
   * </p>
   *
   * @since 4.0 By default the XML Feature {@value #DISABLE_DOCTYP} will be set to true to disable
   *        doctype declarations.
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
   * Wraps {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
   *
   * @since 4.0 If not specified, then the default is 'false' so that we mitigate against XXE
   *        attacks when parsing XML.
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
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
        // INTERLOK-3573
        // Default expand entity ref to false, and then override.
        f.setExpandEntityReferences(false);
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
        // INTERLOK-3573
        // Disable DOCTYPS, and then override.
        f.setFeature(DISABLE_DOCTYPE, true);
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
   *
   * @return a new instance which is basically the same as {@link #newRestrictedInstance()}.
   */
  public static final DocumentBuilderFactoryBuilder newInstance() {
    return newRestrictedInstance();
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
        .addFeature(DISABLE_DOCTYPE, Boolean.TRUE);
  }

  /**
   * Return a DocumentBuilderFactoryBuilder instance that explicitly does not mitigate against XXE
   * and is also namespace aware.
   *
   * <p>
   * This is included for completeness, it's not expected that you'll want to use this, however if
   * you were using {@link #newInstance()} and you don't like the new defaults then well, here it
   * is.
   * </p>
   *
   * @return a new instance
   */
  public static final DocumentBuilderFactoryBuilder newLenientInstance() {
    return new DocumentBuilderFactoryBuilder().withNamespaceAware(true)
        .withExpandEntityReferences(true).addFeature(DISABLE_DOCTYPE, Boolean.FALSE);
  }


  /**
   * Convenient method to create a new default instance if required.
   *
   * @param b an existing configured DocumentBuilderFactoryBuilder, if null, then
   *        {@link #newInstance()} is used.
   *
   * @return a DocumentBuilderFactoryBuilder instance.
   */
  public static final DocumentBuilderFactoryBuilder newInstanceIfNull(DocumentBuilderFactoryBuilder b) {
    return ObjectUtils.defaultIfNull(b, newInstance());
  }

  /**
   * Convenient method to create a new default instance if required.
   *
   * @param b an existing configured DocumentBuilderFactoryBuilder, if null, then
   *        {@link #newRestrictedInstance()} is used.
   *
   * @return a DocumentBuilderFactoryBuilder instance.
   */
  public static final DocumentBuilderFactoryBuilder newRestrictedInstanceIfNull(DocumentBuilderFactoryBuilder b) {
    return ObjectUtils.defaultIfNull(b, newRestrictedInstance());
  }

  /**
   * Convenient method to create a new default instance if required.
   *
   * @param b an existing configured DocumentBuilderFactoryBuilder, if null, then
   *        {@link #newLenientInstance()()} is used.
   *
   * @return a DocumentBuilderFactoryBuilder instance.
   */
  public static final DocumentBuilderFactoryBuilder newLenientInstanceIfNull(DocumentBuilderFactoryBuilder b) {
    return ObjectUtils.defaultIfNull(b, newLenientInstance());
  }

  public static final DocumentBuilderFactoryBuilder newInstanceIfNull(DocumentBuilderFactoryBuilder b, NamespaceContext ctx) {
    if (b != null) {
      return b;
    }
    return ctx == null ? newRestrictedInstance() : newRestrictedInstance().withNamespaceAware(true);
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
