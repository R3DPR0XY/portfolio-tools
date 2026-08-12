# Case Study - Void Sentinel

[Versao em portugues](void-sentinel.md)

## Summary

Void Sentinel is a configuration and design package for an RPG server, with a tank/control class, abilities, balancing, and installation documentation.

## Problem

Creating a custom server class requires more than writing abilities. It is necessary to align theme, balancing, dependencies, installation, testing, and maintenance.

## Solution

The package organizes the Void Sentinel class with visual identity, abilities, YAML files, balancing presets, and testing instructions. The structure separates resources by plugin and makes it easier to adapt the content to different setups.

## Technical Points

- Configurations for MythicMobs, MMOCore, MythicLib, and MMOItems.
- Abilities with damage, control, healing, mitigation, and effects.
- Installation and testing documentation.
- PvE/PvP balancing presets.
- Folder and responsibility separation.

## What It Demonstrates

- Gameplay system design.
- Organization of complex configurations.
- Ability to document dependencies and installation flow.
- Product thinking for reusable technical content.

## Path

[examples/void-sentinel](../../examples/void-sentinel)
