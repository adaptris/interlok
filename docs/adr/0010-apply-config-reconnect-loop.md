---
layout: page
title: 0010-channel-lifecycle
---
# Applying Config during a reconnection loop

* Status: 
* Deciders: Aaron McGrath, Lewin Chan, Paul Higginson, Sebastien Belin
* Date: 2020-10-07


## Context and Problem Statement

As recorded in [INTERLOK-2875](https://adaptris.atlassian.net/browse/INTERLOK-2975) when Interlok is attempting to reconnect to an endpoint it becomes very difficult to amend configuration through the UI.

The reconnect loop is dictated by the reconnection settings with each configured connection.  When a connection is initialized if it cannot connect to it's endpoint the reconnect loop begins.  Connections are initialized on startup and during runtime should it lose it's connection, Interlok will shift the connection through the lifecycle phases until it is initialised again.
Therefore we have two triggers for this issue;
 - A connection cannot be established on startup
 - A connection breaks during runtime.

Until a connection has either established it's connection or has reached the maximum number of configured reconnect attempts, the connection will remain in the initialization phase.

Each phase of an Interlok component is dictated by the StateManagedComponent interface that defines the 4 basic stages of __init__, __start__, __stop__ and __close__.  When we __request__ a change of a components state (from __init__ to __start__ for example) we typically synchronize the state change until it is complete.

This means for example when Interlok launches the initialization phase kicks in which will attempt to initialize each connection.  Any connection that cannot be established as mentioned above will stay in the synchronized __init__ phase potentially forever.  Because the phase transition is synchronized we cannot therefore change this state until it's current transition has completed.

There are currently two problems applying configuration from the UI while a connection is attempting reconnection.

When applying config the UI will request a restart of the running instance, which will attempt to run through the lifecycle of phases mentioned above.
But of course if a connection is stuck in it's synchronized initialization phase trying to reconnect all other state change requests must wait therefore the process hangs.

The second problem when applying config is simply that the UI process will wait until the Interlok process has completed it's full startup, which of course it will not be able to do if a connection cannot be established.  Essentially the process hangs for a period of time.

## Considered Options

### First issue

* Do Nothing
* Remove the synchronization
* Interrupt the blocking transition

### Second issue

 - Do Nothing
 - Non blocking apply of config

## Decision Outcome

### First issue

Chosen option: Not yet decided.

#### Do Nothing

In this case we accept that most people are not modifying production ready instances and even if they are they are probably not using the UI, therefore they are not going to run into the issues raised here.

#### Remove the synchronization

There are a couple of ways to do this.

The first would be to literally remove the synch blocks from implementations like this one;
```java
public final void init() throws CoreException {
    synchronized (lock) {
      if (!prepared) {
        prepare();
      }
      initConnection();
      LifecycleHelper.init(connectionErrorHandler());
    }
  }
```

The second way to do it would be to wrap the actual work inside the synch block with a new thread.

##### Consequences

Allowing a process to request a state change while in the middle of a previous state change could cause undefined behaviour especially around edge cases. 

#### Interrupt the blocking transition

Essentially we would stop the transition currently running, in this case the reconnect inside the initialization phase transition.

This can be done a couple of ways;

First we could make sure the thread handling the initialization transition is a named and managed thread from our ThreadManagerFactory and then add a new static method to that factory that would send an interrupt to that named thread.

Or considering the reconnect code could be refactored into the parent connection impl, we could cover all connections that have reconnect ability, which is all of those that extend AllowsRetriesConnection.  Once we make sure the reconnect code only exists in this parent class we then have a new volatile variable that gets set when a __close__ request is made the reconnect loop could check the value and exit if necessary.

### Second issue

Chosen option: Not yet decided.

#### Do Nothing

In this case we deem the current hanging behaviour as expected and correct.

#### Non-blocking applying of config

In this case a change would be made to the UI application of config where it would request the full restart but not necessarily wait for the running instance to complete it's full start-up.  There would then either be some kind of notification once Interlok has completed the start-up or users would just be directed to the dashboard.