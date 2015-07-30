package com.adaptris.core.services.metadata;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.IdGenerator;
import com.adaptris.util.license.License;
import com.adaptris.util.license.License.LicenseType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Service implementation that generates a unique item of metadata.
 * 
 * <p>
 * Uses the configured {@link IdGenerator} instance to generate a unique value that is subsequently assigned to metadata. Note that
 * this is not designed to replace the unique-id that is associated with an AdaptrisMessage, but is intended to be an additional way
 * for you to generate unique ids that can be associated with a message.
 * </p>
 * 
 * @config generate-unique-metadata-value-service
 * @license BASIC
 */
@XStreamAlias("generate-unique-metadata-value-service")
public class GenerateUniqueMetadataValueService extends ServiceImp {

  private String metadataKey;
  @NotNull
  @AutoPopulated
  private IdGenerator generator;

  public GenerateUniqueMetadataValueService() {
    this(null, new GuidGenerator());
  }

  public GenerateUniqueMetadataValueService(String metadataKey) {
    this(metadataKey, new GuidGenerator());
  }

  public GenerateUniqueMetadataValueService(String metadataKey, IdGenerator generator) {
    setMetadataKey(metadataKey);
    setGenerator(generator);
  }

  /** @see com.adaptris.core.AdaptrisComponent#init() */
  @Override
  public void init() throws CoreException {
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    String metadataKey = metadataKey(msg);
    String metadataValue = getGenerator().create(msg);
    log.trace("Adding [" + metadataValue + "] to metadata key [" + metadataKey + "]");
    msg.addMetadata(metadataKey, metadataValue);
  }

  /** @see com.adaptris.core.AdaptrisComponent#close() */
  @Override
  public void close() {
  }

  /**
   * <p>
   * Returns the metadata key whose value should be checked.
   * </p>
   *
   * @return metadataKey the metadata key whose value should be checked
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Sets the metadata key whose which will store the new value.
   *
   * @param s the metadata key; if set to null, then a unique-key will be generated using the configured
   *          {@link #setGenerator(IdGenerator)}.
   */
  public void setMetadataKey(String s) {
    metadataKey = s;
  }

  String metadataKey(AdaptrisMessage msg) {
    return isEmpty(getMetadataKey()) ? getGenerator().create(msg) : getMetadataKey();
  }

  public IdGenerator getGenerator() {
    return generator;
  }

  /**
   * Set the generator to be used.
   *
   * @param idg the generator; default is {@link GuidGenerator}
   */
  public void setGenerator(IdGenerator idg) {
    if (idg == null) {
      throw new IllegalArgumentException("Generator may not be null");
    }
    generator = idg;
  }

  @Override
  public boolean isEnabled(License license) throws CoreException {
    return license.isEnabled(LicenseType.Basic);
  }
}
