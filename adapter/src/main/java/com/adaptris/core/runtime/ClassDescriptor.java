package com.adaptris.core.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.ChannelList;
import com.adaptris.core.CoreException;
import com.adaptris.core.Service;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.adaptris.core.WorkflowList;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("class-descriptor")
public class ClassDescriptor implements Serializable {
  
  private static final long serialVersionUID = 7470083842357355697L;

  public enum ClassType {
    SERVICE,
    SERVICE_COLLECTION,
    PRODUCER,
    CONSUMER,
    WORKFLOW,
    WORKFLOW_LIST,
    CHANNEL,
    CHANNEL_LIST,
    INTERCEPTOR,
    UNKNOWN;
    
    public static ClassType getTypeForClass(Class<?> clazz) throws CoreException {
      if(ServiceCollection.class.isAssignableFrom(clazz)) {
        return ClassType.SERVICE_COLLECTION;
      } else if(Service.class.isAssignableFrom(clazz)) {
        return ClassType.SERVICE;
      } else if(AdaptrisMessageProducer.class.isAssignableFrom(clazz)) {
        return ClassType.PRODUCER;
      } else if(AdaptrisMessageConsumer.class.isAssignableFrom(clazz)) {
        return ClassType.CONSUMER;
      } else if(WorkflowList.class.isAssignableFrom(clazz)) {
        return ClassType.WORKFLOW_LIST;
      } else if(Workflow.class.isAssignableFrom(clazz)) {
        return ClassType.WORKFLOW;
      } else if(Channel.class.isAssignableFrom(clazz)) {
        return ClassType.CHANNEL;
      } else if(ChannelList.class.isAssignableFrom(clazz)) {
        return ClassType.CHANNEL;
      } else if(WorkflowInterceptor.class.isAssignableFrom(clazz)) {
        return ClassType.INTERCEPTOR;
      } else
        throw new CoreException("Unknown component type: " + clazz.getName());
    }
  }

  private String classType;
  
  private String className;
  
  private String alias;
  
  private String summary;
  
  private String tags;
  
  @XStreamImplicit(itemFieldName="properties")
  private List<ClassDescriptorProperty> classDescriptorProperties;
  
  @XStreamImplicit(itemFieldName="sub-types")
  private List<ClassDescriptor> subTypes;
  
  public ClassDescriptor() {
    classDescriptorProperties = new ArrayList<>();
    subTypes = new ArrayList<>();
  }
  
  public ClassDescriptor(String className) {
    this();
    this.setClassName(className);
  }

  public String getClassType() {
    return classType;
  }

  public void setClassType(String classType) {
    this.classType = classType;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public List<ClassDescriptorProperty> getClassDescriptorProperties() {
    return classDescriptorProperties;
  }

  public void setClassDescriptorProperties(List<ClassDescriptorProperty> classDescriptorProperties) {
    this.classDescriptorProperties = classDescriptorProperties;
  }

  public List<ClassDescriptor> getSubTypes() {
    return subTypes;
  }

  public void setSubTypes(List<ClassDescriptor> subTypes) {
    this.subTypes = subTypes;
  }
}
