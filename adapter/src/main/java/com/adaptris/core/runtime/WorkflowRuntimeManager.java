package com.adaptris.core.runtime;

import com.adaptris.core.Workflow;

public interface WorkflowRuntimeManager extends AdapterRuntimeComponent<Workflow>, ChildComponent<ChannelManager>,
    ParentRuntimeInfoComponent {

}
