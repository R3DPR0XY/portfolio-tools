---
name: r3dpr0xy-forge-maps
description: "Create original R3DPR0XY Forge Maps: branded technical showcase maps for Minecraft mods, server plugins, Java repositories, GitHub Actions pipelines, release flows, and portfolio case studies. Use when Codex needs to turn a project into a recognizable R3DPR0XY-style public presentation asset, especially for READMEs, docs, releases, Modrinth/Spigot pages, GitHub profiles, or portfolio pages."
---

# R3DPR0XY Forge Maps

Create a Forge Map: a public-facing technical map that explains what a project is, how it runs, how it builds, and what users get from it.

This is not a generic diagram system. A Forge Map should feel like an R3DPR0XY artifact: direct, technical, product-like, and built for developers who want their work to look release-ready.

## Core Format

Every map chooses one of four original layouts:

- **Release Rail**: trigger -> build -> package -> artifact -> public release.
- **Runtime Core**: player/admin action -> entrypoint -> services -> state -> visible result.
- **Mod Circuit**: loader -> entrypoint -> mixins/screens/assets -> user-facing feature.
- **Showcase Board**: project value in the center, surrounded by features, stack, proof, and release output.

Read the matching reference before creating a map:

- `references/release-rail.md`
- `references/runtime-core.md`
- `references/mod-circuit.md`
- `references/showcase-board.md`

## Visual Identity

Use the R3DPR0XY Forge skin:

- `void`: `#0f1117` for dark structural ink
- `panel`: `#f4f1ea` for warm technical paper
- `steel`: `#475569` for secondary strokes
- `signal`: `#e23d28` for the single focal path
- `reactor`: `#00a6a6` for public output or external integrations
- `ash`: `#d7d1c5` for rules and muted panels

Rules:

- Use one red `signal` path per map.
- Use teal `reactor` only for external/public output.
- Use compact rectangular badges, not pills.
- Use no shadows, no glow, no decorative gradients.
- Prefer grid, rails, slots, cores, ports, and output labels as the visual vocabulary.

Typography:

- Title: strong sans or serif fallback, 28-40px.
- Node name: sans, 12-14px, semibold.
- Technical detail: mono, 8-10px.
- Badges: uppercase mono, 7-8px.

## Workflow

1. Inspect the project files first.
2. Identify the audience: user, developer, client, recruiter, or maintainer.
3. Pick one layout from `Core Format`.
4. Keep 5-9 meaningful blocks.
5. Save a standalone HTML file under `docs/forge-maps/` unless instructed otherwise.
6. Include inline SVG, embedded CSS, accessible `<title>` and `<desc>`.
7. Report the output path and the facts used from the repo.

## Quality Bar

A Forge Map is successful when a visitor can understand the project in 10 seconds:

- What starts the system?
- What does the project do?
- What is technically inside?
- What artifact or user-visible result comes out?
- Why should this project look credible?

If the map only shows boxes connected by arrows, revise it into a rail, circuit, core, or board.
