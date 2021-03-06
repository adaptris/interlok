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

package com.adaptris.util.text.mime;


/** Just some constants that are used by Mime.
 */
public interface MimeConstants {

  /** Mime Header corresponding to Content-Id */
  String HEADER_CONTENT_ID = "Content-Id";
  /** Mime Header corresponding to Content-Type */
  String HEADER_CONTENT_TYPE = "Content-Type";
  /** Mime Header corresponding to Message-ID */
  String HEADER_MESSAGE_ID = "Message-ID";
  /** Mime Header corresponding to Content-Transfer-Encoding */
  String HEADER_CONTENT_ENCODING = "Content-Transfer-Encoding";
  /** Mime Header corresponding to Mime-Version */
  String HEADER_MIME_VERSION = "Mime-Version";
  /** Mime Header corresponding to Content-Description */
  String HEADER_CONTENT_DESC = "Content-Description";
  /** Mime Header corresponding to Content-Length */
  String HEADER_CONTENT_LENGTH = "Content-Length";
  /** base64 Encoding type */
  String ENCODING_BASE64 = "base64";
  /** uuencode Encoding type */
  String ENCODING_UUENCODE = "uuencode";
  /** 7bit Encoding type */
  String ENCODING_7BIT = "7bit";
  /** 8bit Encoding type */
  String ENCODING_8BIT = "8bit";
  /** quoted-printable Encoding type */
  String ENCODING_QUOTED = "quoted-printable";
  /** binary Encoding type */
  String ENCODING_BINARY = "binary";

  String CONTENT_TYPE_WWW_FORM_URLENCODE = "application/x-www-form-urlencoded";
  String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
  String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
}
