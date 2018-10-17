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

package com.adaptris.mail;

/** Mail Constants.
 * 
 * @author lchan
 * @author $Author: lchan $
 */
public interface Mail {

  /** The Content Encoding header */
  String CONTENT_ENCODING = "Content-Transfer-Encoding";
  /** The Content type header */
  String CONTENT_TYPE = "Content-Type";
  /** The Content dispostition Header */
  String CONTENT_DISPOSITION = "Content-disposition";
  /** The type of disposition */
  String DISPOSITION_TYPE_ATTACHMENT = "attachment";
  /** A parameter to content disposition */
  String DISPOSITION_PARAM_FILENAME = "filename";
}
