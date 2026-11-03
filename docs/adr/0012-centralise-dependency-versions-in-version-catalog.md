---
layout: page
title: centralise dependency versions in a shared version catalog
---
# Centralise dependency versions in a shared Gradle version catalog

* Status: accepted
* Deciders: Interlok development team
* Date: 2026-09-29

Technical Story: We want a single source of truth for dependency versions across the Interlok projects and downstream repositories, while keeping upgrades easy to review and automate with tools such as Dependabot.

## Context and Problem Statement

Interlok is a multi-module project with multiple repositories depending on the same shared dependency stack. Across these projects, dependency versions were being repeated in multiple Gradle build files.

This created several problems:

* version drift between modules and repositories
* inconsistent upgrades across the same dependency family
* noisy review churn when updating shared libraries
* manual effort required to keep upgrades aligned across projects
* increased risk of regressions caused by different dependency versions being used in different places

We wanted a simple approach that a small team could operate without a lot of build maintenance overhead. We also wanted to make routine upgrades easier to automate and review with Dependabot and GitHub Actions.

## Decision Drivers

* We want a single source of truth for dependency versions.
* We want low operational overhead for a small team.
* We want weekly dependency checks and upgrades to be easy to automate.
* We want each project to consume versions consistently without repeating literals in every build file.
* We want downstream Interlok repositories to consume a published, stable platform without local project references.
* We do not want to create a complex dependency-management model for a small team if a simpler catalog/BOM pattern will do.

## Considered Options

* Keep dependency versions in each Gradle build file.
* Use a single shared Gradle version catalog TOML file for all shared versions.
* Use a single shared TOML plus a published BOM/platform artifact for downstream repositories.
* Use a per-project custom dependency management model with local overrides everywhere.

## Decision Outcome

Chosen option: "Use a single shared Gradle version catalog TOML file, published as a BOM/platform artifact for downstream repositories".

This approach gives us a single source of truth for our shared dependency versions while still being simple to operate. The version catalog is the canonical place where shared versions are declared. The project and downstream consumers resolve the dependency set through a published platform artifact instead of hard-coding versions into each build.

We will keep project-specific or truly local dependency versions out of the catalog unless they are genuinely reused across multiple repositories. This keeps the catalog useful without creating unnecessary complexity.

### Positive Consequences

* Shared versions are managed in one place.
* Upgrades are easier to review and automate with Dependabot.
* Repository build files stay simpler and easier to read.
* Downstream projects can consume the shared platform without local project references.
* It reduces the chance of version drift across related repositories.
* It keeps the maintenance model aligned with a small team and a pragmatic delivery pace.

### Negative consequences

* The catalog is a shared dependency contract and must be reviewed carefully before changing versions.
* Upgrading a shared library can affect multiple repositories at once.
* Not every dependency should live in the shared catalog; this requires discipline to avoid over-centralising project-specific choices.
* Publishing the platform correctly is required before downstream repositories can consume the latest versions.

## Pros and Cons of the Options

### Keep dependency versions in each Gradle build file

* Good, because it is the simplest thing to start with.
* Good, because each project can evolve independently.
* Bad, because versions drift over time.
* Bad, because dependency upgrades become repetitive and hard to track across projects.
* Bad, because small teams end up doing a lot of duplicate version maintenance.

### Use a single shared version catalog TOML file

* Good, because it provides a single source of truth.
* Good, because it is easy for Dependabot and automation to review and update.
* Good, because it reduces duplication in Gradle files.
* Bad, because it still requires disciplined ownership and a clear rule for what belongs in the catalog.
* Bad, because it is less useful unless consumers actually use the catalog consistently.

### Use a single shared TOML plus a published BOM/platform artifact

* Good, because it provides the cleanest dependency model for multi-repo adoption.
* Good, because downstream repositories can consume the platform without local references.
* Good, because it works well with CI and release automation.
* Good, because it supports easy dependency reviews and regular upgrades.
* Bad, because it requires a publish/release workflow for the catalog artifact.
* Bad, because version bumps must be coordinated with downstream repos when they adopt a new BOM version.

### Use a per-project custom dependency management model

* Good, because it is simple in the short term.
* Good, because it avoids a central catalog structure.
* Bad, because it scales poorly as the number of repositories and modules grows.
* Bad, because it leads to hidden drift and inconsistent upgrades.
* Bad, because it makes repeat upgrades harder across the ecosystem.

## Links

* [ADR-0000](0000-use-markdown-architectural-decision-records.md)
* [Interlok root build](../../build.gradle)
* [Interlok version catalog project](../../interlok-version-catalog)
