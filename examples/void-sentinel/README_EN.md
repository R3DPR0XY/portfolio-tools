# Void Sentinel Pack

[Versao em portugues](README.md)

Tank/control class inspired by the Watcher's Shield style, but more aggressive: orbital shields, pull, stun, mitigation, healing, area damage, and collapse ultimate.

Visual theme: black, red, and white.

## Target Dependencies

- MythicMobs 5.x
- MMOCore
- MythicLib

Optional:

- MMOItems or MythicCrucible for custom item/weapon support
- ItemsAdder, Nexo, or Oraxen for better icons/models/VFX

## Class Identity

Name: Void Sentinel
Role: frontline control tank
Palette:

- Black: void, singularity, defense
- Red: judgment, damage, collapse
- White: shield, healing, sentinel

## Installation

1. Copy `MythicMobs/Skills/void_sentinel_skills.yml` to:
   `plugins/MythicMobs/Skills/void_sentinel_skills.yml`

2. Copy `MMOCore/classes/void-sentinel.yml` to:
   `plugins/MMOCore/classes/void-sentinel.yml`

3. Copy `MythicLib/skill/void-sentinel.yml` to:
   `plugins/MythicLib/skill/void-sentinel.yml`

4. Optional: copy `MMOItems/void-sentinel-items.yml` to your MMOItems/MythicCrucible item folder, adjusting the path for your setup.

5. Restart the server or run plugin reloads:
   - `/mm reload`
   - `/ml reload`
   - `/mmocore reload`

6. Test first through MythicMobs/MythicLib:
   - `/mm debug cast VoidSentinel_ShieldThrow`
   - `/ml debug cast SHIELD_THROW`

The `MMOCore/skills/void-sentinel.yml` file is included as a draft/compatibility file for older setups, but the recommended flow in current documentation is registering skills through MythicLib.

## Kit

- `Orbital Resonance`: simple sustain passive. Heals and grants light absorption.
- `Shield Throw`: throws shield energy, deals damage, slows, pulls, and marks the target.
- `Void Guard`: defensive skill. Grants resistance/absorption and later explodes with knockback.
- `Singularity Chain`: mini black hole. Pulls, slows, and deals damage over ticks.
- `Warden's Verdict`: frontal/short-area slam with stun.
- `Event Horizon`: ultimate. Pulls enemies, stuns, and deals burst damage.

## Suggested Binds

- LMB: Shield Throw
- RMB: Void Guard
- Shift + LMB: Singularity Chain
- Shift + RMB: Warden's Verdict
- Double Shift or ultimate key: Event Horizon

## Quick Balancing

For PvE:

- Increase damage in `damage{a=...}`.
- Use stun between 20 and 35 ticks.
- Use shorter cooldowns in MMOCore.

For PvP:

- Reduce `stun{d=...}` to 10-20 ticks.
- Reduce `@EIR{r=...}` radius.
- Increase `EVENT_HORIZON` cooldown.

## Notes

This pack uses vanilla particles so it works without paid assets. The visual layer can later be replaced with custom models/VFX for a premium look.

Some option names may vary by MythicMobs/MMOCore version. If a reload reports an error, use the exact console log to adjust the files for your version.
