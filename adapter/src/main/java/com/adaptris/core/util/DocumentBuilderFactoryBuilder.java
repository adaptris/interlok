package com.adaptris.core.util;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(DocumentBuilderFactoryBuilder.class);

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
        log.trace("{} additional features", b.getFeatures().size());
        for (KeyValuePair entry : b.getFeatures()) {
          f.setFeature(entry.getKey(), BooleanUtils.toBoolean(entry.getValue()));
          // log.debug("{} is now {}", entry.getKey(), f.getFeature(entry.getKey()));
        }
      }
    };
    abstract void applyConfig(DocumentBuilderFactoryBuilder b, DocumentBuilderFactory f) throws ParserConfigurationException;
  }

  public DocumentBuilderFactoryBuilder() {
    features = new KeyValuePairSet();
  }

  public static final DocumentBuilderFactoryBuilder newInstance() {
    return new DocumentBuilderFactoryBuilder();
  }

  public DocumentBuilderFactory configure(DocumentBuilderFactory f) throws ParserConfigurationException {
    for (FactoryConfiguration c : FactoryConfiguration.values()) {
      c.applyConfig(this, f);
    }
    return f;
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
    this.features = Args.notNull(features, "Features");;
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

  public Boolean getValidating() {
    return validating;
  }

  /**
   * Maps to {@link DocumentBuilderFactory#setValidating(boolean)}.
   */
  public void setValidating(Boolean validate) {
    this.validating = validate;
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
}
