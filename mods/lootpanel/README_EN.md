# LootPanel

[Versao em portugues](README.md)

Client-side Fabric mod base for Minecraft Java 1.21.8.

## What Is Already Done

- Opens together with container screens, such as chests.
- Sums identical items inside the open container.
- Ignores the player's inventory.
- Draws a panel with:
  - item icon;
  - item name below the icon;
  - total count below the name.

## Main Files

- `src/main/java/com/bmod/chestpanel/mixin/HandledScreenMixin.java`: injects the panel at the end of container screen rendering.
- `src/main/java/com/bmod/chestpanel/client/ChestPanelRenderer.java`: collects items and draws the panel.
- `src/main/resources/fabric.mod.json`: mod manifest.
- `gradle.properties`: Minecraft/Fabric versions.

## How To Build

Install JDK 21 and run:

```powershell
gradle build
```

The `.jar` is generated in:

```text
build/libs/
```

Then place the `.jar` in the `mods` folder together with Fabric Loader and Fabric API for Minecraft 1.21.8.

## Where To Change The Visuals

In `ChestPanelRenderer.java`, adjust:

- `PANEL_WIDTH`
- `CELL_WIDTH`
- `CELL_HEIGHT`
- colors `BACKGROUND`, `BORDER`, `TEXT_COLOR`, `COUNT_COLOR`
- `x` and `y` positioning inside the `render` method

## Notes

The project is structured for publication as a small, focused client-side utility.
