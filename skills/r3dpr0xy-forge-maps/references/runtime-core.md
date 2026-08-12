# Runtime Core

Use for Bukkit, Spigot, Paper, Velocity, BungeeCord, and server-side systems.

## Inspect

- `plugin.yml`
- `pom.xml` or Gradle build
- commands
- listeners
- services
- stores/configs
- permissions
- placeholders or external hooks

## Layout

Use a central core with four sides:

- left: player/admin input
- top: server events
- center: plugin core/service layer
- right: visible gameplay result
- bottom: storage/config/integrations

The core is the focal shape. It should explain the plugin's value, not just its class structure.

## Labels

Use names a server owner understands:

- `Player command`
- `Admin command`
- `Event listener`
- `Service core`
- `Config store`
- `Placeholder hook`
- `Gameplay result`

## Finish

Add a compact proof strip:

- platform
- Java version
- build tool
- generated artifact
