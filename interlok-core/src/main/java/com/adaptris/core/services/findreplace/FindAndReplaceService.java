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

package com.adaptris.core.services.findreplace;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.BooleanUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * Implementation of {@link com.adaptris.core.Service} which allows find and replace operations on the message payload.
 * <p>
 * You can specify one or more {@link com.adaptris.core.services.findreplace.FindAndReplaceUnit}'s, each of which defines the value
 * to "find" and the value to "replace" with. Each {@link com.adaptris.core.services.findreplace.FindAndReplaceUnit} will be applied
 * in order and can be used to replace the first instance or all instances.
 * </p>
 * 
 * @config find-and-replace-service
 * 
 * @see ConfiguredReplacementSource
 * @see MetadataReplacementSource
 */
@XStreamAlias("find-and-replace-service")
@AdapterComponent
@ComponentProfile(summary = "Perform a find and replace on the message", tag = "service")
@DisplayOrder(order = {"findAndReplaceUnits", "replaceFirstOnly"})
public class FindAndReplaceService extends ServiceImp {

  @XStreamImplicit(itemFieldName = "find-replace-pair")
  @Valid
  private List<FindAndReplaceUnit> findAndReplaceUnits;
  @InputFieldDefault(value = "false")
  private Boolean replaceFirstOnly;

  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public FindAndReplaceService() {
    findAndReplaceUnits = new ArrayList<FindAndReplaceUnit>();
  }

  /**
   * @see com.adaptris.core.Service #doService(com.adaptris.core.AdaptrisMessage)
   */
  public void doService(AdaptrisMessage msg) throws ServiceException {
    for (FindAndReplaceUnit unit : getFindAndReplaceUnits()) {
      String find = unit.getFind().obtainValue(msg);
      String replace = unit.getReplace().obtainValue(msg);
      log.trace("replacing [" + find + "] with [" + replace + "]");
      doReplace(msg, find, replace);
    }
  }

  private void doReplace(AdaptrisMessage msg, String toReplace, String replaceWith) {
    // Standard AdaptrisMessages provide Null Production for msgs.getStringPayload()
    String result = null;
    if (replaceFirstOnly()) {
      result = msg.getContent().replaceFirst(toReplace, replaceWith);
    }
    else {
      result = msg.getContent().replaceAll(toReplace, replaceWith);
    }
    msg.setContent(result, msg.getContentEncoding());
  }

  @Override
  protected void initService() throws CoreException {
  }

  @Override
  protected void closeService() {

  }


  // getters and setters

  /**
   * <p>
   * Returns replaceFirstOnly.
   * </p>
   *
   * @return replaceFirstOnly if true only the first instances of the <code>String</code> to replace will be replaced. If false all
   *         instances will be replaced.
   */
  public Boolean getReplaceFirstOnly() {
    return replaceFirstOnly;
  }

  /**
   * <p>
   * Sets replaceFirstOnly.
   * </p>
   * 
   * @param b if true only the first instances of the <code>String</code> to replace will be replaced. If false all instances will
   *          be replaced; default is null (false)
   */
  public void setReplaceFirstOnly(Boolean b) {
    replaceFirstOnly = b;
  }

  boolean replaceFirstOnly() {
    return BooleanUtils.toBooleanDefaultIfNull(getReplaceFirstOnly(), false);
  }

  public List<FindAndReplaceUnit> getFindAndReplaceUnits() {
    return findAndReplaceUnits;
  }

  public void setFindAndReplaceUnits(List<FindAndReplaceUnit> findAndReplaceUnit) {
    this.findAndReplaceUnits = findAndReplaceUnit;
  }

  @Override
  public void prepare() throws CoreException {
  }
}
