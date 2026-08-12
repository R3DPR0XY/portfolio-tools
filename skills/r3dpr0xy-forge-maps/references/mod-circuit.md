# Mod Circuit

Use for Fabric mods, client utilities, HUDs, GUI screens, mixins, resource packs, and mod build outputs.

## Inspect

- `fabric.mod.json`
- `build.gradle`
- `gradle.properties`
- entrypoint classes
- mixin config
- screens/HUD/rendering classes
- assets and lang files

## Layout

Use a circuit path with ports:

1. `LOADER`
2. `ENTRYPOINT`
3. `PATCH`
4. `FEATURE`
5. `ASSET`
6. `JAR`

Not every map needs every port. Keep the user-facing feature as the largest block.

## Labels

Use readable labels:

- `Fabric loader`
- `Client init`
- `Mixin patch`
- `HUD / Screen`
- `Lang + icon`
- `Remapped JAR`

## Finish

Add a version chip row:

- Minecraft version
- Fabric Loader
- Fabric API
- mod version
