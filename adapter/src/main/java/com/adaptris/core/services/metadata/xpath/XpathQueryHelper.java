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

package com.adaptris.core.services.metadata.xpath;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.text.xml.XPath;

final class XpathQueryHelper {

  static String resolveSingleTextItem(Document doc, NamespaceContext ctx, String expr, boolean allowEmptyResults)
      throws CoreException {
    XPath xp = new XPath(ctx);
    String queryResult = null;
    try {
      String node = xp.selectSingleTextItem(doc, expr);
      if (StringUtils.isEmpty(node) && !allowEmptyResults) {
        throw new Exception(expr + " returned no data");
      }
      queryResult = node;
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return queryResult;
  }

  static String resolveMultipleTextItems(Document doc, NamespaceContext ctx, String expr, boolean allowEmptyResults, String sep)
      throws CoreException {
    XPath xp = new XPath(ctx);
    String queryResult = null;
    try {
      String[] nodes = xp.selectMultipleTextItems(doc, expr);
      if (isEmpty(nodes) && !allowEmptyResults) {
        throw new CoreException(expr + " returned [" + nodes.length + "] nodes");
      }
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < nodes.length; i++) {
        result.append(nodes[i]);
        if (i < nodes.length - 1) {
          result.append(sep);
        }
      }
      queryResult = result.toString();
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return queryResult;
  }

  static Node resolveSingleNode(Document doc, NamespaceContext ctx, String expr, boolean allowNull) throws CoreException {
    XPath xp = new XPath(ctx);
    Node queryResult = null;
    try {
      queryResult = xp.selectSingleNode(doc, expr);
      if (queryResult == null && !allowNull) {
        throw new CoreException("Query [" + expr + "] return null");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return queryResult;
  }

  static NodeList resolveNodeList(Document doc, NamespaceContext ctx, String expr, boolean allowNull) throws CoreException {
    XPath xp = new XPath(ctx);
    NodeList queryResult = null;
    try {
      queryResult = xp.selectNodeList(doc, expr);
      if (queryResult == null && !allowNull) {
        throw new CoreException("Query [" + expr + "] returned null");
      }
    }
    catch (Exception e) {
      ExceptionHelper.rethrowCoreException(e);
    }
    return queryResult;
  }

  private static final boolean isEmpty(String[] list) {
    return list.length == 0;
  }

}
