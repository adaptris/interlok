---
layout: page
title: 0011-annotation-aware-lifecycle
---
# Automatically enforcing lifecycle via annotations

* Status: Accepted
* Deciders: Lewin Chan, Aaron McGrath, Sebastien Belin
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

## Decision Outcome

Annotation + LifecycleHelper runtime behavioural change. Introduction of a `InterlokLifecycle` annotation.

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

![Sequence](https://www.plantuml.com/plantuml/png/TL5DZwem5DtVNt7YHbt8ssimchTuBjuaPlv1WOjqKbgclPZuxwTyZ0Q26xZqz7pSmoc8yjBM1gl_kkJyaHi-M-j4YNPsHTH4MEUwKLPmLPwjJ8wUf0kRXlq4-rjfKqnEFhZEuYLAKtosLdJDk7gNSmZQtf2cD3ZiZZFP_TmdQJn7OTxadSUMPl3ks7llyOy5QbY72zIzVQ3upJ1S2bmvfknoP2ep3zorWkVISDur40x8MY7SEY5Z81N_oiHP2IfrPyBwei8pm_fbUI2SJN3m_Drgpm4AJPiPdKSzOVydz2zYMo0oc5GqlKVYcAf_BhHndYQXWcV4TttCQZFEDIYTZ_Hg52FW3RdhOFY1coq1GxQ5S9wbzHOk-rhI6XHUIUArJksPVy09ux1aa2H9n5yxz_Lrgz0fJcrjw0S0)


* Good, no change to existing code, since that won't have annotations.
* Good, because we just need to have the annotation on the member.
* Bad, init() might end up being called multiple times unless the component is StateManaged.
* Bad, slower on startup?
* Bad, if the graph is self-referential then stack overflow.

Should all components be StateManaged?
