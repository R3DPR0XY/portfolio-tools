# Hero of Shield

Fabric mod for Minecraft 1.21.8 focused on visual awareness, directional threat cues, aura effects, and utility overlays.

## Current Scope

Habilidade Unica currently provides:
- threat and awareness scans for nearby entities
- off-screen directional indicators
- aura bubble visuals with optional shared particle sync
- HUD telemetry, block inspector, furnace overlays, and inventory insight
- food and custom-content tooltips
- Mod Menu integration and JSON-based configuration

The gameplay-facing systems remain client-driven. A lightweight common/server bootstrap exists only to receive aura sync packets and render shared particles for other players when that option is enabled.

## Project Layout

```text
src/main/java/com/draxxlink/unique_skill/
|- UniqueSkill.java
|- client/
|  |- combat/
|  |- effect/
|  |- hud/
|  |- integration/
|  |- inventory/
|  |- network/
|  |- render/
 |  |- screen/
|  \- tooltip/
|- config/
|- entity/
|- network/
|- server/
\- state/
```

## Configuration

The mod creates `config/unique_skill.json` automatically.

Key options include:
- `modEnabled`
- `toggleKey`
- `autoAttackPlayers`
- `autoAttackAimToleranceDegrees`
- `detectionRange`
- `alertVolume`
- `showMessages`
- `showHud`

## Build

On Windows:

```powershell
.\gradlew.bat build
```

On Unix-like shells:

```bash
./gradlew build
```

Artifacts are generated under `build/libs/`.

