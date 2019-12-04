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

package com.adaptris.core.fs;

import java.util.List;

import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.Service;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.stubs.MockChannel;

public class FsNullDestinationConsumerTest {
	
	 protected StandardWorkflow createWorkflowForGenericTests() {
	    return new StandardWorkflow();
	  }
	 
	protected MockChannel createChannel(AdaptrisMessageProducer producer, List<Service> services) throws Exception {
	    MockChannel channel = new MockChannel();
	    StandardWorkflow workflow = createWorkflowForGenericTests().setConsumer(param);;
	    workflow.getConsumer().setDestination(new ConfiguredConsumeDestination("dummy"));
	    workflow.setProducer(producer);
	    workflow.getServiceCollection().addAll(services);
	    channel.getWorkflowList().add(workflow);
	    return channel;
	  }
	

	 
}