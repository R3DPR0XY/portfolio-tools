# BrewBloom

[Versao em portugues](README.md)

Client-side Fabric mod for Minecraft that adds cosmetic bubbles around the player while potion/status effects are active.

## Concept

Minecraft already uses small colored particles to show active effects. BrewBloom turns that idea into a more deliberate visual aura: soft particles orbit around the player, with colors derived from active effects or custom palettes.

## Features

- Cosmetic only.
- Client-side.
- Spawns colored potion-style bubbles while status effects are active.
- Supports multiple simultaneous effects.
- Supports effect colors, neon animation, rainbow animation, or up to 3 manual colors.
- Multiple movement styles.
- Multiple bubble texture styles.
- In-game configuration menu.
- Button sounds using native Minecraft sounds.
- Toggle key: `B`.
- Config menu key: `G`.
- Config file: `config/brewbloom.json`.

## Configuration

```json
{
  "enabled": true,
  "colorMode": "effect",
  "colors": ["#FF2A2A", "#FFFFFF", "#7A0000"],
  "radius": 0.38,
  "density": 2,
  "effectLimit": 4,
  "heightScale": 0.9,
  "riseSpeed": 1.0,
  "swirlSpeed": 1.0,
  "colorCycleSpeed": 8,
  "menuSounds": true
}
```

Color modes:

- `effect`: uses the original Minecraft status effect color.
- `rgb`: cycles through strong neon colors.
- `rainbow`: cycles through a softer rainbow palette.
- `custom`: cycles between 1 to 3 colors from `colors`.

## Requirements

- Minecraft 1.21.8
- Fabric Loader 0.16.5+
- Fabric API
- Java 21
- Mod Menu optional

## Build

```powershell
.\gradlew.bat build
```

The release jar is generated in:

```text
build/libs/
```

## Modrinth Positioning

Short description:

```text
Configurable cosmetic bubbles for active potion effects.
```

Tags:

```text
Fabric, Client-side, Cosmetic, Particles, Utility, Decoration
```

## Status

Experimental first version.
