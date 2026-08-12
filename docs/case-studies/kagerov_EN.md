# Case Study - Kagerov

[Versao em portugues](kagerov.md)

## Summary

Kagerov is a Fabric mod for Minecraft focused on client-side text, reading, editing, and interface tools. The project shows custom screen creation, integration with game resources, and modular feature organization.

## Problem

Minecraft's default text/book editing and reading flow is limited for creative and roleplay use. It lacks library features, advanced editing, text palettes, and tools that make writing more practical.

## Solution

The mod adds screens and utilities to improve books, signs, and text usage. The structure separates responsibilities between clipboard, library, editor/reader screens, and visual selectors.

## Technical Points

- Fabric mod with client and main entrypoints.
- Mixins to integrate features into existing screens.
- Custom screens for editor, reader, library, and palette.
- JSON resources for languages and visual configuration.
- Package organization by domain: `book`, `screen`, `mixin`.

## What It Demonstrates

- Ability to create UX inside a limited environment.
- Code organization in a client-side project.
- Knowledge of Fabric, mixins, and Minecraft resources.
- Productivity-tool thinking for users.

## Path

[mods/kagerov](../../mods/kagerov)
