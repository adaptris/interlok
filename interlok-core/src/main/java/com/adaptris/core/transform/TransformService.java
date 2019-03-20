/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package com.adaptris.core.transform;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.transform.Source;
import com.adaptris.transform.Target;
import com.adaptris.transform.TransformFramework;

/**
 * <p>
 * Behaviour common to <code>Service</code>s which transform messages.
 * </p>
 * @author sellidge
 * @author lchan
 */
public abstract class TransformService extends ServiceImp {

  private String url;
  @AdvancedConfig
  @AutoPopulated
  private String metadataKey;
  private transient List<Source> cachedRules;
  private transient Source configuredRule;
  private transient TransformFramework tf;
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean cacheTransforms;
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  private Boolean allowOverride;
  private String outputMessageEncoding;

  /**
   * <p>
   * Creates a new instance. Defaults to caching transforms and disallowing
   * metadata-based over-riding of the transform to apply.
   * </p>
   */
  public TransformService() {
    super();
    setMetadataKey(CoreConstants.TRANSFORM_OVERRIDE);
    cachedRules = new ArrayList<Source>();
  }

  @Override
  protected final void initService() throws CoreException {
    try {
      tf = createFramework();

      if (url != null) {
        configuredRule = new Source(url);
        if (cacheTransforms()) {
          tf.addRule(configuredRule);
          cachedRules.add(configuredRule);
        }
      } else {
        log.info("No configured URL, implicit #setAllowOverride(true): " + "transform must be specified by metadata");
        setAllowOverride(true);
      }
    } catch (Exception e) {
      throw new CoreException(e);
    }
  }

  @Override
  protected final void closeService() {
    Iterator i = cachedRules.iterator();
    while (i.hasNext()) {
      Source src = (Source) i.next();
      tf.removeRule(src);
    }
  }

  /**
   * <p>
   * Transforms the message.  If <code>getAllowOverride</code> is true and
   * a transform is specified in message metdata, this over-ride transform will
   * be applied instead of any configured transform.  If no transform is
   * explicitly configured and no over-ride transform is specified in metadata,
   * a <code>ServiceException</code> is thrown.
   * </p>
   * @see com.adaptris.core.Service#doService(AdaptrisMessage)
   */
  @Override
  public final void doService(AdaptrisMessage msg) throws ServiceException {

    try (Reader in = msg.getReader(); Writer output = msg.getWriter(getOutputMessageEncoding())) {
      Source src = new Source(in);
      Target dst = new Target(output);

      Source currentRule = configuredRule;

      if (allowOverride() && msg.headersContainsKey(getMetadataKey())) {
        String url = msg.getMetadataValue(getMetadataKey());
        log.debug("Metadata transform override : {}", url);
        currentRule = new Source(url);
        tf.addRule(currentRule);
        if (cacheTransforms()) {
          cachedRules.add(currentRule);
        }
      }

      if (currentRule != null) {
        tf.transform(src, currentRule, dst);
        if (!cacheTransforms()) {
          tf.removeRule(currentRule);
        }
      }
      else {
        throw new ServiceException("no transform could be applied");
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }


  @Override
  public void prepare() throws CoreException {
  }

  /**
   * <p>
   * Sets the URL of the transformation style sheet.
   * </p>
   *
   * @param s the URL of the transformation style sheet
   */
  public void setUrl(String s) {
    url = s;
  }

  /**
   * <p>
   * Returns the URL of the transformation style sheet.
   * </p>
   * @return the URL of the transformation style sheet
   */
  public String getUrl() {
    return url;
  }

  /**
   * <p>
   * Return the flag indicating whether transforms should be cached or not.
   * </p>
   * @return the flag indicating whether transforms should be cached or not.
   */
  public Boolean getCacheTransforms() {
    return cacheTransforms;
  }

  /**
   * <p>
   * Sets the flag indicating whether transforms should be cached or not.
   * </p><p>
   * By default, transforms are cached for the purposes of optimisation.
   * However, in the event that you wish to constantly change transforms, this
   * flag can be set false to disable this feature.
   * </p>
   * @param b the flag indicating whether transforms should be cached or not
   */
  public void setCacheTransforms(Boolean b) {
    cacheTransforms = b;
  }

  boolean cacheTransforms() {
    return BooleanUtils.toBooleanDefaultIfNull(getCacheTransforms(), true);
  }

  /**
   * <p>
   * Sets the flag specifying whether or not an 'over-ride' transform may be
   * specified in message metadata.
   * </p><p>
   * By default, it is not possible to override the given transform with one
   * specified via metadata, if this behaviour is desired, then this flag should
   * be set true. If not transform is configured using <code>setUrl</code>, this
   * flag is set to true in <code>init</code>.
   * </p>
   * @param b the flag specifying whether or not an 'over-ride' transform may be
   * specified in message metadata
   */
  public void setAllowOverride(Boolean b) {
    allowOverride = b;
  }

  /**
   * <p>
   * Returns the flag specifying whether or not an 'over-ride' transform may be
   * specified in message metadata.
   * </p>
   * @return the flag specifying whether or not an 'over-ride' transform may be
   * specified in message metadata
   */
  public Boolean getAllowOverride() {
    return allowOverride;
  }

  boolean allowOverride() {
    return BooleanUtils.toBooleanDefaultIfNull(getAllowOverride(), true);
  }
  /**
   * <p>
   * Sets the metadata key that will be used to store the over-ride transform
   * URL.  This URL will only be used if <code>allowOverride</code> is set to
   * true.
   * </p>
   * @param s the metadata key that will be used to store the over-ride
   * transform URL
   */
  public void setMetadataKey(String s) {
    metadataKey = s;
  }

  /**
   * <p>
   * Returns the metadata key that will be used to store the over-ride transform
   * URL.
   * </p>
   * @return the metadata key that will be used to store the over-ride transform
   * URL
   */
  public String getMetadataKey() {
    return metadataKey;
  }

  public String getOutputMessageEncoding() {
    return outputMessageEncoding;
  }

  /**
   * Force the output message encoding to be a particular encoding.
   * <p>
   * If specified then the underlying {@link com.adaptris.core.AdaptrisMessage#setCharEncoding(String)} is changed to match the encoding specified
   * here before attempting any write operations.
   * </p>
   * <p>
   * This is only useful if the underlying message is encoded in one way, and you wish to force the encoding directly in your ff
   * transformation definition; e.g. the input message is physically encoded using ISO-8859-1; but your xslt has &lt;<root
   * encoding="utf-8"> and you need to ensure that the message is physically encoded using UTF-8
   * </p>
   * 
   * @param s
   */
  public void setOutputMessageEncoding(String s) {
    outputMessageEncoding = s;
  }

  /**<p>
   * Create the appropriate transform framework.
   * </p>
   * @return a transformframework
   * @throws Exception on error.
   */
  protected abstract TransformFramework createFramework() throws Exception;
}
