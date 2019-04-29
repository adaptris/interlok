---
layout: page
title: 0002-add-default-implementations-to-componentlifecycle
---
# Add Default methods to ComponentLifecycle interface

* Status: Accepted
* Deciders: Aaron McGrath, Lewin Chan
* Date: 2019-03-07

## Context and Problem Statement

Because ComponentLifecycle defines 4 abstract methods; it isn't possible to have an interface that extends ComponentLifecycle and yet marked with @FunctionalInterface, since functional interfaces may only have a single abstract method defined.

## Considered Options

* Add default implementions to ComponentLifecycle
* Leave it as it is.

## Decision

Add default implementation code.

### Add default implementions to ComponentLifecycle

By adding default implementations it makes them non-abstract; this means that we can do interesting functional interface things by defining additional interfaces that extend ComponentLifecycle directly.

```
@FunctionalInterface
public interface MessageHandler extends ComponentLifecycle {
  public AdaptrisMessage handleMessage(AdaptrisMessage);
}
```

Although Service isn't eligible to become a functional interface since it extends other things; it would have been ideal for this, as ultimately we want to get to the stage that we can define services as simply as  `(m) -> { System.err.println(m.getUniqueId()); }`.

### Leave it as it is.

We leave it as it is, which means we don't change any existing behaviour. Concrete implementations of ComponentLifecycle will still need to manually implement init/start/stop/close()

## Consequences

* None of the existing tests break, so this is a low-risk change
* Things that extend ServiceImp will still have to implement closeService() / initService() since they are explicitly abstract.
* We will need to go through existing implementations that have empty methods for init/start/stop/close just to clean them up.
* We could mark some other interfaces with @FunctionInterface since they only define a single method, and extend ComponentLifecycle directly
  * AccessTokenBuilder
  * IdentityBuilder
  * IdentityVerifier
  * PollingTrigger.MessageProvider


