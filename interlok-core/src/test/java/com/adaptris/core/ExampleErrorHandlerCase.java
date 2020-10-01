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

import com.adaptris.annotation.Removal;
import com.adaptris.core.stubs.UpgradedToJunit4;

/**
 * <p>
 * Extension to <code>BaseCase</code> for <code>Service</code>s which
 * provides a method for marshaling sample XML config.
 * </p>
 */

@Deprecated
@Removal(version = "4.0.0", message = "moved to com.adaptris.interlok.junit.scaffolding")
public abstract class ExampleErrorHandlerCase
    extends com.adaptris.interlok.junit.scaffolding.ExampleErrorHandlerCase
    implements UpgradedToJunit4 {

}
