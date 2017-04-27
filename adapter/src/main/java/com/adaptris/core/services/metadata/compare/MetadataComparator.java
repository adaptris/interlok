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

package com.adaptris.core.services.metadata.compare;

import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;

/** Compare two items of metadata returning the result of the comparison.
 * 
 * @author lchan
 *
 */
public interface MetadataComparator {

  MetadataElement compare(MetadataElement firstItem, MetadataElement secondItem) throws ServiceException;
}
