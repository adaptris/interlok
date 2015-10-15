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

package com.adaptris.core;


/**
 * <p>
 * Applies arbitrary processing to <code>AdaptrisMessage</code>s.  Applicable
 * where the calling code does not want to be informed
 * of any <code>Exception</code>s that may occur.  <code>Service</code>
 * is applicable where the container requires to be informed of 
 * <code>Exception</code>s.
 * </p>
 * @see Service
 */
public interface AdaptrisMessageListener {

  /**
   * <p>
   * It is the responsibility of implementations of this interface to ensure 
   * that all <code>Exception</code>s, including <code>RuntimeException</code>s,
   * are handled.  Failure to handle any <code>Exception</code> will result in 
   * undefined behaviour.  Throwing a <code>RuntimeException</code> to this 
   * method is considered a bug.  
   * </p><p>
   * Although most clients of implementations of this interface are likely to 
   * be single-threaded, if implementations are not guaranteed to be thread 
   * safe, they should be <code>synchronized</code> or use some other locking
   * mechanism. 
   * </p>
   * @param msg the <code>AdaptrisMessage</code> to process
   */
  void onAdaptrisMessage(AdaptrisMessage msg);
}
