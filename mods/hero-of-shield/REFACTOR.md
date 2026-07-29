# Hero of Shield - Refactor Notes

## Current state

- `UniqueSkillClient` remains the main gameplay/runtime entrypoint for Habilidade Única client features.
- `UniqueSkill` now registers a common initializer for packet types and shared aura support in Habilidade Única.
- `fabric.mod.json` includes both `main` and `client` entrypoints.
- Mod Menu integration remains client-only.

## What changed from the earlier client-only pass

- common bootstrap was restored for packet registration
- lightweight server support was added for optional shared aura particles
- common payload definitions now live under `network/`
- the project structure expanded to include overlays, inventory insight, and content helpers

The server-side piece is intentionally narrow: it does not alter combat rules, attributes, or authority. It only mirrors cosmetic aura state when clients opt into sharing it.

## What remains client-driven

- threat detection for nearby visible threats
- directional indicators and HUD updates
- local attack input automation
- aura visuals and config-driven presentation
- block, furnace, inventory, and tooltip overlays
- JSON config loading/saving
- Mod Menu config screen

## Technical notes

- awareness and threat scans are cached briefly to reduce per-tick cost
- directional indicators use short hold windows for smoother feedback
- aura sharing is packet-based and throttled on the client
- the current Gradle build assembles distributable jars and does not ship automated tests yet

## Recommended verification

1. Run `.\gradlew.bat build`.
2. Place the jar from `build/libs/` into a Fabric 1.21.8 client.
3. Open the mod config through Mod Menu and confirm settings persist.
4. Join a world and verify HUD, overlays, alerts, auto attack, and aura visuals.
5. If testing shared aura mode, join with another compatible client and confirm particles are mirrored.
