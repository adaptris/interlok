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

package com.adaptris.core.management;


/**
 * Entry point into an adapter from the commandline.
 * <p>
 * Basically the same as StandardBootstrap but without the classpath initialization.
 * </p>
 *
 * @author gcsiki
 */
public class SimpleBootstrap extends StandardBootstrap {

	public SimpleBootstrap() throws Exception {
		super(new String[0]);
		super.boot();
	}
	
  public SimpleBootstrap(String[] argv) throws Exception {
    super(argv);
  }

  @Override
  public void boot() throws Exception {
    logVersionInformation();
    super.boot();
  }

  /**
   * <p>
   * Entry point to program.
   * </p>
   *
   * @param
   * 		argv - Command line arguments
   *
   * @throws Exception upon some unrecoverable error.
   */
  public static void main(String[] argv) throws Exception {
    new SimpleBootstrap(argv).boot();
  }
}
