---
layout: page
title: 0005-remove-produce-destination
---
# Deprecate and ultimately remove Destination

* Status: Accepted
* Deciders: Lewin Chan, Aaron McGrath, Paul Higginson, Sebastien Belin, Matt Warman
* Date: 2019-07-30

## Context and Problem Statement

If we consider something like `jms-topic-producer` then we have to configure a produce destination what contains the topic. Traditionally, if we wanted a metadata driven destination, then it would have to be done via a `metadata-destination` but now with the `%message{metadata-key}` expression language; we would just use configured-produce-destination everywhere. However; this isn't necessarily obvious to the user. Additionally, we cannot have a destination that isn't a String.

There are some producers that have 2 mandatory pieces of information that would be considered the _destination_. For instance the AWS Kinesis producer requires both a _stream-name_ and a _partition-key_. This kind of requirement doesn't lend itself very well to a `produce-destination`; we can't make the destination return a delimited string since both of those values are essentially arbitrary. So we either completely ignore produce-destination, or we use the destination for the stream name, and then something else entirely for the partition key.

There are already some producers that ignore the destination entirely: SAP Idoc producer, Jetty Response Producer are the two that immediately spring to mind. So if isn't mandatory, then why have it?

EDIT: 2020-06-16 The proposal is to also remove `ConsumeDestination` as well as `ProduceDestination`

## Decision Drivers

* Less confusion for new users.
* Clean up configuration to take advantage of new features.
* Make things more predictable.

## Considered Options

* The status quo is always an option.
* Deprecate ProduceDestination
* Introduce alternative implementations to ProduceDestination.

## Decision Outcome

Deprecate and Remove

### Status Quo

We're in the army now.

#### Consequences

* Good, because there's no work to do, apart from new producers that don't fit the existing produce-destination paradigm.
* Bad, because we aren't simplifying the object model and configuration.

### Deprecate Destination

If we deprecate Destination then we need to

* Deprecate destination as a member in all producers and consumers (AdaptrisMessageWorker as well?)
* Ensure that all producers and consumers provide an alternative method for specifying the destination
    * For FsConsumer we might have a "directory-name" that is a String -> `%message{directory}`
    * Ditto FsProducer
    * It might be different for other producer/consumer types
* Deprecate the produce/request methods in the main producer interface.
    * Default it to "null", and make sure it's not NotNull
    * this means that the UI will no longer display the destination when you create a new producer in the UI; but if you have it configured, it will be present with the deprecated warnings.

#### JmsReplyToDestination

Finally there is a special `JmsReplyToDestination` that is in use; this uses object metadata to derive the `javax.jms.Destination` (and is handled specially by the JMS Producer Impls).

* Change the JMS Producer implementations so that if the object metadata exists (use-jms-reply-to-if-available=true), then it uses the JmsReplyTo value.
* Deprecate JmsReplyToDestination

#### Consequences

* Neutral, because there will be crossover period where both styles need to be valid; and how do we message this change to the existing users without a massive kerfuffle.
* Good, it ultimately simplifies the configuration, leaving us "less work" with ConfigBean translations.
* Good, because it allows producers to define their own configuration, in fact having a non-string based destination.
* __*Neutral/Bad, because it's changes the produce/request contract in AdaptrisMessageProducer*__

### Additional ProduceDestination implementations

For the Kinesis produce we could introduce a new `KinesisProduceDestination` that contains an additional getPartitionKey() method (or we make it generic, and add a getQualifier() method).

#### Consequences

* Good, because we aren't modifying the object model
* Bad, because we have to cast ProduceDestination
* Bad, because we still have to support the situation where the user has only configured a ConfiguredProduceDestination or similar.
