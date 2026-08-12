# Case Study - LootPanel

[Versao em portugues](lootpanel.md)

## Summary

LootPanel is a client-side Fabric mod that displays an item panel when opening containers. It groups identical items, ignores the player's inventory, and presents the container contents more clearly.

## Problem

Large containers can be slow to analyze visually, especially when many repeated items are present. Players need to count manually and compare slots.

## Solution

The mod injects a panel into container screens and renders a consolidated item list. The panel shows icon, name, and total quantity, reducing reading effort.

## Technical Points

- Mixins on container screens.
- Item stack reading and aggregation.
- Custom panel rendering.
- Separate configuration for behavior and layout.
- Organized Gradle/Fabric project for builds.

## What It Demonstrates

- Ability to turn a user pain point into a small, useful tool.
- Client-side rendering knowledge.
- Inventory data manipulation without changing game rules.
- Organization of a simple, focused, publishable mod.

## Path

[mods/lootpanel](../../mods/lootpanel)
