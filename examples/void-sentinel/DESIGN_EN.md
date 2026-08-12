# Void Sentinel Design

[Versao em portugues](DESIGN.md)

## Fantasy

A cosmic guardian who fights with four orbital shields. It wins by controlling distance, grouping enemies, and converting defense into explosions.

Main palette:

- Black: vacuum, smoke, portal, reverse portal
- Red: judgment, damage, collapse, crimson particles
- White: shields, healing, sentinel, protection flashes

## Main Rotation

1. `Singularity Chain` to group enemies.
2. `Warden's Verdict` for a short stun.
3. `Shield Throw` on the priority target.
4. `Void Guard` when focused.
5. `Event Horizon` when multiple enemies are grouped.

## Future Upgrade: Real Shield System

The MVP does not use a real four-shield resource to avoid breaking across different plugin versions.

Suggested premium version:

- Create a `vs_shields` variable from 0 to 4.
- Each skill consumes shields.
- A metaskill regenerates 1 shield every X seconds.
- If `vs_shields` is 0, defensive skills fail or become weaker.

## Suggested Premium VFX

- Four orbital shields with ModelEngine.
- `Shield Throw`: physical shield flying out and returning.
- `Singularity Chain`: black/purple sphere with rings.
- `Void Guard`: shields closing into a dome.
- `Event Horizon`: visual collapse with a circular wave.

## "Better Than Watcher" Version

The original Watcher is more unique because of shield management. Void Sentinel wins through:

- More real stun.
- More direct sustain.
- More finishing damage.
- Clear pull -> stun -> burst rotation.
- More aggressive black/red/white visual theme.
- Easy adaptation for PvP or PvE.
