# CreditsRewards

[Versao em portugues](README.md)

Commercial Paper plugin for Minecraft servers with a credits system, missions, rewards, NPC shops, and database persistence.

## Status

Closed/commercial project. This directory is a public showcase of the product; source code, builds, and complete configurations are not included in this repository.

## Overview

CreditsRewards was created for servers that need their own economy based on progression, missions, and controlled rewards. The system centralizes credits, shops, stock, missions, visual feedback, and optional integrations in a single plugin.

## Main Features

- Custom credits system.
- YAML-configurable missions.
- Progress-based rewards.
- Shops opened through Citizens NPCs.
- Global stock and per-player limits.
- Transaction history.
- SQLite or MySQL persistence.
- Feedback through sounds, ActionBar, and BossBar.
- Optional Nexo support.
- Optional PlaceholderAPI support.

## Mission Types

- `KILL`
- `BUILD`
- `MINE`
- `CRAFT`
- `FISH`
- `DELIVER`

## Command Examples

Player:

```text
/creditos
/creditos top
/missoes
```

Administration:

```text
/creditos admin give <player> <amount>
/creditos admin take <player> <amount>
/creditos admin set <player> <amount>
/recompensas reload
/recompensas lojas
/recompensas abrir <shop>
/soulsociety status
/soulsociety validate
```

## Requirements

- Paper 1.21.11
- Java 21
- Citizens
- SQLite or MySQL

Optional:

- Nexo
- PlaceholderAPI
- TAB

## Licensing

This plugin is not distributed as free software in this repository.

Usage, editing, redistribution, resale, source-code copying, reverse engineering, and creation of derivative versions require separate commercial authorization/license.

## Why The Code Is Not Public

CreditsRewards is treated as a commercial product. For that reason, this repository shows the technical capability, scope, and documentation of the project, while preserving source code and builds for private/licensed distribution.

## Distribution

For sale or private delivery, the recommended format is:

- Compiled `.jar`.
- Commercial license terms.
- Per-version changelog.
- Installation guide.
- Configuration guide.
- Defined support channel.

## Note

Obfuscation can make direct copying harder, but it does not replace licensing, contracts, controlled distribution, and professional support.
