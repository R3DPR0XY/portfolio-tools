# Case Study - MPersonagem

[Versao em portugues](mpersonagem.md)

## Summary

MPersonagem is a Paper plugin for roleplay servers. It allows players to create, edit, and view characters with name, surname, age, height, gender, description, menus, and permissions.

## Problem

Roleplay servers need persistent player identity, but many solutions depend on manual processes, scattered commands, or configurations that are hard to maintain.

## Solution

The plugin centralizes character creation and editing through commands and menus. Data is persisted per player, messages are configurable, and server staff have administrative commands for lookup and maintenance.

## Technical Points

- Paper plugin with Java 21.
- Player and administration commands.
- Interactive inventory menus.
- UUID-based file persistence.
- Permission system.
- Configuration through `config.yml` and `messages.yml`.
- Optional PlaceholderAPI/TAB hooks.

## What It Demonstrates

- Backend development for servers.
- Business-rule organization.
- Persistence and editable configuration.
- Care for user and administration flows.
- More complete project with multiple functionality layers.

## Path

[plugins/mpersonagem](../../plugins/mpersonagem)
