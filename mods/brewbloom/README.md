# BrewBloom

[English version](README_EN.md)

Mod Fabric client-side para Minecraft que adiciona bolhas cosméticas ao redor do jogador enquanto efeitos de poção/status estão ativos.

## Concept

Minecraft already uses small colored particles to show active effects. BrewBloom turns that idea into a more deliberate visual aura: soft particles orbit around the player, with colors derived from active effects.

## Features

- Cosmetic only.
- Client-side.
- Spawns colored potion-style bubbles while status effects are active.
- Supports multiple simultaneous effects.
- Supports effect colors, RGB animation, rainbow animation, or up to 3 custom colors.
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
- `rgb`: cycles through RGB colors quickly.
- `rainbow`: cycles through a softer rainbow palette.
- `custom`: cycles between 1 to 3 colors from `colors`.

## Requirements

- Minecraft 1.21.8
- Fabric Loader 0.16.5+
- Fabric API
- Java 21

## Build

```powershell
.\gradlew.bat build
```

The release jar is generated in:

```text
build/libs/
```

## Modrinth Positioning

Descrição curta:

```text
Bolhas cosméticas e configuráveis para efeitos de poção ativos.
```

Tags:

```text
Fabric, Client-side, Cosmetic, Particles, Utility, Decoration
```

## Status

Experimental first version.
