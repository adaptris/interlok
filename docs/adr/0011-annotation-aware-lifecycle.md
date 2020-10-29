---
layout: page
title: 0011-annotation-aware-lifecycle
---
# Automatically enforcing lifecycle via annotations

* Status: PROPOSAL
* Deciders: Lewin Chan, Matt Warman, Aaron McGrath, Sebastien Belin, Paul Higginson
* Date: 2020-10-29

## Context and Problem Statement

It's probably easiest to describe this using an example. Consider the changes that were implemented for [ADR-0008](0008-restful-failed-message-retrier.md). This added a new interface

```java
public interface RetryStore extends ComponentLifecycle, ComponentLifecycleExtension {
  // some methods.
}
```

along with a configurable item that uses the new pluggable interface

```java
public class RetryStoreWriteService extends ServiceImp {
  /**
   * Where messages are stored for retries.
   *
   */
  @Getter
  @Setter
  @NotNull
  @NonNull
  private RetryStore retryStore;
}
```

This class requires a lot of boiler plate along the lines of overriding parent methods / implementing missing methods.

```java
  @Override
  public void prepare() throws CoreException {
    Args.notNull(getRetryStore(), "retry-store");
    LifecycleHelper.prepare(getRetryStore());
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getRetryStore());

  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(getRetryStore());
    super.start();

  }

  @Override
  public void stop() {
    LifecycleHelper.stop(getRetryStore());
    super.stop();

  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getRetryStore());
  }
```

This is prone to mistakes and we should be removing the boilerplate where possible.

## Decision Drivers

* Backwards compatible; i.e. doesn't break existing code.
* Low friction to uptake

## Considered Options

* Do Nothing
* Annotation + Compile time code generator
* Annotation + LifecycleHelper runtime behavioural change
* ... <!-- numbers of options can vary -->

## Decision Outcome

Proposed but undecided.

## Pros and Cons of the Options

### Do Nothing

Making no changes is the status quo

* Good, because no work
* Bad, because you always forget to override all the methods.

### Compile time code generator

Since we are already using lombok, is there a way that we can plugin with lombok so that we actually _generate_ the required boiler plate code in the same way that it generates getters and setters.

Add an annotation `HasLifecycle` that is then parsed by the javac plugin to automatically generate the correct boilerplate init/start/stop/close methods.

```java
public class RetryStoreWriteService extends ServiceImp {
  @Getter
  @Setter
  @NotNull
  @NonNull
  @HasLifecycle
  private RetryStore retryStore;
}
```

* Good, because no change to existing code.
* Good, because we won't need to write any init/start/stop/close methods.
* Bad, probably hugely complicated but we could still do this in the same way core-apt works if needs be.
* Bad, because forced behaviour might be impossible.
* Bad, because ServiceImp implicitly breaks it since it has final methods that can't be overridden.

### Runtime processing of annotations.

We keep the `HasLifeycle` annotation, but we modify LifecycleHelper so that when `Lifecycle.prepare(object)` is called; it gets all the annotations on the object's members; if there is a `HasLifecycle` annotation, and a corresponding getter + setter then it can automatically call recursively call `LifecycleHelper.prepare(object.getMember())`.

So effectively the sequence is

![Sequence](http://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.githubusercontent.com/adaptris/interlok/ADR-0011-lifecycle-annotations/docs/adr/assets/0011-runtime-annotation.puml)


* Good, no change to existing code, since that won't have annotations.
* Good, because we just need to have the annotation on the member.
* Bad, init() might end up being called multiple times unless the component is StateManaged.
* Bad, slower on startup?
* Bad, if the graph is self-referential then stack overflow.

Should all components be StateManaged?
