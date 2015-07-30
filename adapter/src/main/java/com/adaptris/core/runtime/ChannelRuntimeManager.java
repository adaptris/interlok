package com.adaptris.core.runtime;

import com.adaptris.core.Channel;

public interface ChannelRuntimeManager extends AdapterRuntimeComponent<Channel>, ParentComponent<WorkflowRuntimeManager>,
    ChildComponent<AdapterRuntimeManager>,
    ParentRuntimeInfoComponent {

}
