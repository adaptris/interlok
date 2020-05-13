---
layout: home
title: index
---
# Architectural Decision Log

This log lists the architectural decisions for Interlok

<!-- adrlog -->

- [ADR-0000](0000-use-markdown-architectural-decision-records.md) - Use Markdown Architectural Decision Records
* [ADR-0001](0001-add-messagelogger-interface.md) - Add MessageLogger interface, remove AdaptrisMessage.toString()
* [ADR-0002](0002-add-default-implementations-to-componentlifecycle.md) - Add Default methods to ComponentLifecycle interface
* [ADR-0003](0003-deprecate-dynamicservicelocator.md) - Deprecate DynamicServiceLocator
* [ADR-0004](0004-remove-jndi-bindable-lookup-name.md) - Deprecate and ultimately remove JndiBindable#getLookupName()
* [ADR-0005](0005-remove-produce-destination.md) - Deprecate and ultimately remove ProduceDestination interface.
* [ADR-0006](0006-workflow-callback.md) - Make onAdaptrisMessage() have a callback
* [ADR-0007](0007-jetty-failsafe.md) - Allow jetty management component to run w/o any configuration

<!-- adrlogstop -->

For new ADRs, please use [template.md](template.md) as basis.
More information on MADR is available at <https://adr.github.io/madr/>.
General information about architectural decision records is available at <https://adr.github.io/>.
