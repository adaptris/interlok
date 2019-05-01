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

import static org.apache.commons.lang.StringUtils.isBlank;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.CastorizedList;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Container for a collection of {@link com.adaptris.core.Channel} objects
 * </p>
 * 
 * @config channel-list
 */
@XStreamAlias("channel-list")
@AdapterComponent
@ComponentProfile(summary = "A Collection of Channels", tag = "base")
public class ChannelList extends AbstractCollection<Channel>
    implements ComponentLifecycle, List<Channel>,
    ComponentLifecycleExtension {
  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Valid
  @NotNull
  @AutoPopulated
  @XStreamImplicit
  private List<Channel> channels;
  private transient Map<String, Channel> addressableChannels;
  @Valid
  @AdvancedConfig
  private ChannelLifecycleStrategy lifecycleStrategy;

  
  /**
   * <p>
   * Creates a new instance.
   * </p>
   */
  public ChannelList() {
    channels = new ArrayList<Channel>();
    addressableChannels = new HashMap<String, Channel>();
  }

  public ChannelList(Collection<Channel> coll) {
    this();
    addAll(coll);
  }

  /**
   * Make sure we are ready for initialisation.
   *
   * @throws CoreException
   */
  @Override
  public void prepare() throws CoreException {
    addressableChannels.clear();
    for (Channel c : channels) {
      // Register the channel against the addressable channel list.
      register(c);
      c.prepare();
    }
  }

  private Channel register(Channel element) {
    if (element.hasUniqueId()) {
      String id = element.getUniqueId();
      if (addressableChannels.containsKey(id)) {
        throw new IllegalArgumentException("duplicate Channel ID [" + id + "]");
      }
      else {
        addressableChannels.put(id, element);
      }
    }
    return element;
  }

  private Channel unregister(Channel element) {
    if (element.hasUniqueId()) {
      addressableChannels.remove(element.getUniqueId());
    }
    return element;
  }

  /**
   * <p>
   * Uses the the configured {@link ChannelLifecycleStrategy} to invoke {@link StateManagedComponent#requestInit()} on the
   * underlying <code>Channel</code>s in the <code>List</code>.
   * </p>
   *
   * @throws CoreException wrapping any underlying <code>Exception</code> s
   */
  @Override
  public void init() throws CoreException {
    log.trace("Channels that can be manipulated are: " + addressableChannels.keySet());
    lifecycleStrategy().init(channels);

  }

  /**
   * <p>
   * Uses the the configured {@link ChannelLifecycleStrategy} to invoke {@link StateManagedComponent#requestStart()} on the
   * underlying <code>Channel</code>s in the <code>List</code>.
   * </p>
   *
   * @throws CoreException wrapping any underlying <code>Exception</code>s
   */
  @Override
  public void start() throws CoreException {
    lifecycleStrategy().start(channels);

  }

  /**
   * <p>
   * Uses the the configured {@link ChannelLifecycleStrategy} to invoke {@link StateManagedComponent#requestStop()} on the
   * underlying <code>Channel</code>s in the <code>List</code>.
   * </p>
   */
  @Override
  public void stop() {
    lifecycleStrategy().stop(channels);

  }

  /**
   * <p>
   * Uses the the configured {@link ChannelLifecycleStrategy} to invoke {@link StateManagedComponent#requestClose()} on the
   * underlying <code>Channel</code>s in the <code>List</code>.
   * </p>
   *
   */
  @Override
  public void close() {
    lifecycleStrategy().close(channels);
  }

  /**
   * <p>
   * Returns the underlying <code>List</code> of <code>Channel</code>s.
   * </p>
   *
   * @return the underlying <code>List</code> of <code>Channels</code>
   */
  public List<Channel> getChannels() {
    return new CastorizedList<Channel>(this);
  }

  /**
   * <p>
   * Set the underlying <code>List</code> of <code>Channel</code>s.
   * </p>
   *
   * @param l the underlying <code>List</code> of <code>Channels</code>
   */
  public void setChannels(List<Channel> l) {
    channels = Args.notNull(l, "channels");
    addressableChannels.clear();
    for (Channel c : l) {
      register(c);
    }
  }

  /**
   * <p>
   * Adds a <code>Channel</code> to the underlying <code>List</code> and validates its unique ID.
   * </p>
   *
   * @param channel the <code>Channel</code> to add
   */
  public void addChannel(Channel channel) {
    add(channel);
  }

  /**
   * <p>
   * Returns the <code>Channel</code> stored at the passed <code>pos</code> in the <code>List</code>.
   * </p>
   *
   * @param pos the position in the <code>List</code>
   * @return the <code>Channel</code> stored at the passed <code>pos</code> in the <code>List</code>
   */
  public Channel getChannel(int pos) {
    return channels.get(pos);
  }

  /**
   * <p>
   * Returns the number of <code>Channel</code> s in this <code>ChannelList</code>.
   * </p>
   *
   * @return the number of <code>Channel</code> s in this <code>ChannelList</code>
   */
  @Override
  public int size() {
    return channels.size();
  }

  /**
   * <p>
   * Returns the {@link com.adaptris.core.Channel} with the passed <code>uniqueId</code> or null if no such channel exists.
   * </p>
   *
   * @param uniqueId the unique ID of the Channel to return, may not be null or empty
   * @return the requested Channel or null if it doesn't exist, will always be null unless {@link #prepare()} has been called.
   */
  public Channel getChannel(String uniqueId) {
    if (isBlank(uniqueId)) {
      throw new IllegalArgumentException("illegal param [" + uniqueId + "]");
    }
    Channel result = addressableChannels.get(uniqueId);
    return result;
  }

  /**
   * @return the startStrategy
   */
  public ChannelLifecycleStrategy getLifecycleStrategy() {
    return lifecycleStrategy;
  }

  /**
   * Specify the strategy to use when handling channel lifecycle.
   * <p>
   * If not explicitly specified, then {@link DefaultChannelLifecycleStrategy} will be used to handle the channel operations.
   * </p>
   *
   * @param css the strategy to set
   * @see DefaultChannelLifecycleStrategy
   * @see com.adaptris.core.lifecycle.NonBlockingChannelStartStrategy
   */
  public void setLifecycleStrategy(ChannelLifecycleStrategy css) {
    lifecycleStrategy = css;
  }

  ChannelLifecycleStrategy lifecycleStrategy() {
    return getLifecycleStrategy() != null ? getLifecycleStrategy() : new DefaultChannelLifecycleStrategy();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public Iterator<Channel> iterator() {
    return channels.listIterator();
  }

  @Override
  public boolean remove(Object o) {
    if (o != null && Channel.class.isAssignableFrom(o.getClass())) {
      return removeChannel((Channel) o);
    }
    return false;
  }

  /**
   * Remove a channel from this channel list.
   * 
   * @param channel
   * @return true if the channel was removed successfully.
   */
  public boolean removeChannel(Channel channel) {
    if (channel == null) {
      return false;
    }
    boolean result = channels.remove(channel);
    unregister(channel);
    return result;
  }

  @Override
  public boolean add(Channel element) {
    return channels.add(register(Args.notNull(element, "channel")));
  }

  @Override
  public void add(int index, Channel element) {
    channels.add(index, register(Args.notNull(element, "channel")));
  }

  @Override
  public boolean addAll(int index, Collection<? extends Channel> c) {
    for (Channel element : Args.notNull(c, "channels")) {
      register(element);
    }
    return channels.addAll(index, c);
  }

  @Override
  public Channel get(int index) {
    return channels.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return channels.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return channels.lastIndexOf(o);
  }

  @Override
  public void clear() {
    channels.clear();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<Channel> listIterator() {
    return channels.listIterator();
  }

  /**
   * {@inheritDoc}
   *
   */
  @Override
  public ListIterator<Channel> listIterator(int index) {
    return channels.listIterator(index);
  }

  @Override
  public Channel remove(int index) {
    return unregister(channels.remove(index));
  }

  @Override
  public Channel set(int index, Channel element) {
    unregister(channels.get(index));
    return channels.set(index, register(element));
  }

  @Override
  public List<Channel> subList(int fromIndex, int toIndex) {
    return channels.subList(fromIndex, toIndex);
  }

}
