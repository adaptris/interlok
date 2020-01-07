package com.adaptris.interlok.client;

import java.io.Serializable;

/**
 * Specifies the target workflow for a message.
 * 
 */
public class MessageTarget implements Serializable {

  private static final long serialVersionUID = 2015082401L;

  private String adapter;
  private String channel;
  private String workflow;

  public String getAdapter() {
    return adapter;
  }

  public void setAdapter(String adapter) {
    this.adapter = adapter;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getWorkflow() {
    return workflow;
  }

  public void setWorkflow(String workflow) {
    this.workflow = workflow;
  }

  /**
   * Convenience method for method chaining.
   * 
   * @param s the adapter name.
   * @return the current {@link MessageTarget}.
   * @see #setAdapter(String)
   */
  public MessageTarget withAdapter(String s) {
    setAdapter(s);
    return this;
  }

  /**
   * Convenience method for method chaining.
   * 
   * @param s the channel name.
   * @return the current {@link MessageTarget}.
   * @see #setChannel(String)
   */
  public MessageTarget withChannel(String s) {
    setChannel(s);
    return this;
  }


  /**
   * Convenience method for method chaining.
   * 
   * @param s the workflow name.
   * @return the current {@link MessageTarget}.
   * @see #setWorkflow(String)
   */
  public MessageTarget withWorkflow(String s) {
    setWorkflow(s);
    return this;
  }


}