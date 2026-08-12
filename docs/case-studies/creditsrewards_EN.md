# Case Study - CreditsRewards

[Versao em portugues](creditsrewards.md)

## Summary

CreditsRewards is a commercial Paper plugin for Minecraft servers. The project combines a custom economy, configurable missions, rewards, NPC shops, inventory stock, transaction history, and SQLite or MySQL persistence.

## Problem

RPG and roleplay servers often need a controlled economy that does not depend only on generic money. They also need missions, shops, rewards, and persistent progress without relying on many disconnected plugins.

## Solution

The plugin centralizes the progression flow:

- Players complete missions.
- Missions generate credits and rewards.
- Credits are used in NPC shops.
- Purchases and balances are persisted.
- Administrators control economy, stock, missions, and validation.

## Technical Points

- Paper plugin with Java 21.
- SQLite or MySQL persistence.
- YAML configuration.
- Citizens integration for NPC shops.
- Optional Nexo support for custom items.
- Optional PlaceholderAPI support.
- Administrative command system.
- Transaction history and logs.
- Structure prepared for a real server.

## Publishing Decision

CreditsRewards was treated as a commercial project. For that reason, the portfolio publishes only product documentation and the case study. Source code, builds, and complete configurations should remain private.

## What It Demonstrates

- Backend development for servers.
- Economy and progression modeling.
- Data persistence.
- Plugin integration.
- Administrative command design.
- Commercial product thinking.
- Separation between public portfolio and private intellectual property.

## Path

[plugins/creditsrewards](../../plugins/creditsrewards)
