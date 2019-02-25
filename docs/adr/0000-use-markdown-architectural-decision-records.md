---
layout: page
title: 0000-use-markdown-architectural-decision-records
---
# Use Markdown Architectural Decision Records

* Status: Accepted
* Deciders: Sebastien Belin, Paul Higginson, Lewin Chan
* Date: 2019-01-24

## Context and Problem Statement

We want to record architectural decisions made in this project.
Which format and structure should these records follow?

## Decision Drivers

* Recording architectural decisions in confluence tend to get lost.

## Considered Options

* [MADR](https://adr.github.io/madr/) 2.1.0 - The Markdown Architectural Decision Records
* [Michael Nygard's template](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions) - The first incarnation of the term "ADR"
* Other templates listed at <https://github.com/joelparkerhenderson/architecture_decision_record>

## Decision Outcome

Chosen option: "MADR 2.1.0", because

* Implicit assumptions should be made explicit.
  Design documentation is important to enable people understanding the decisions later on.
* The MADR format is lean and fits our development style.
* The MADR structure is comprehensible and facilitates usage & maintenance.
* The MADR project is vivid.
* Version 2.1.0 is the latest one available when starting to document ADRs.
