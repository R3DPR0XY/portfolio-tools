# MPersonagem

[Versao em portugues](README.md)

Paper plugin for roleplay character systems with name, surname, age, height, gender, description, menus, height-based attributes, and PlaceholderAPI/TAB support.

## Requirements

- Paper 1.21.x
- Java 21
- PlaceholderAPI optional
- TAB optional

## Player Commands

- `/personagem` opens the selection menu and starts guided character creation.
- `/id` shows your identity: name, age, height, and roleplay data.
- `/id <player>` shows another player's identity if staff has permission.
- `/pularano` adds +1 year to every saved character.

## Recommended Admin Command

Use `/mpersonagem ajuda` to see staff options.

- `/mpersonagem reload` reloads `config.yml` and `messages.yml`.
- `/mpersonagem ver <player>` opens another player's identity.
- `/mpersonagem editar <player>` opens editing for another player's active character.
- `/mpersonagem reset <player>` deletes all characters from a player.
- `/mpersonagem setaltura <player> <height>` sets the active character height.

There is no `/mpersonagem setidade`. Age should be edited through the character menu or advanced in bulk with `/pularano`.

## Creation Flow

Character creation follows a menu sequence:

1. Talent menu.
2. Height menu with attribute scaling preview.
3. Name and age menu.
4. Final confirmation menu.

Name and surname still use chat input because Minecraft inventories do not have native text fields. After the player types, they automatically return to the correct menu.

## Textures

The project includes pixel-art reference textures for resource pack/GUI usage:

- `criacao_personagem_zombie_pixel_256.png`: recommended base texture.
- `criacao_personagem_zombie_pixel_1024.png`: same texture scaled with nearest-neighbor, without smoothing.

Textures are located in `src/main/resources/assets/mpersonagem/textures/gui/`.

## Click-Based Admin Menu

When using `/mpersonagem editar <player>`, staff can open the active character admin menu. This menu allows click-based changes:

- age: `-1` and `+1`
- height: `-5 cm` and `+5 cm`
- talent: rotates through configured talents

## Permissions

- `mpersonagem.use`
- `mpersonagem.view`
- `mpersonagem.edit`
- `mpersonagem.reload`
- `mpersonagem.reset`
- `mpersonagem.setaltura`
- `mpersonagem.pularano`
- `mpersonagem.admin`

## Placeholders

With PlaceholderAPI installed:

- `%mpersonagem_nome%`
- `%mpersonagem_sobrenome%`
- `%mpersonagem_nome_completo%`
- `%mpersonagem_idade%`
- `%mpersonagem_altura%`
- `%mpersonagem_altura_formatada%`
- `%mpersonagem_altura_metros%`
- `%mpersonagem_genero%`
- `%mpersonagem_descricao%`
- `%mpersonagem_tem_personagem%`

For TAB, mainly use `%mpersonagem_nome_completo%` or `%mpersonagem_nome%` in the tablist format. The plugin also updates `playerListName` directly when `settings.update-tab-list-name` is enabled.

## Configuration

Main files:

- `config.yml`: limits, attributes, validations, menu, and nick/TAB formatting.
- `messages.yml`: all editable messages.
- `players/<uuid>.yml`: saved data for each player.

Characters can be created, edited, and deleted without fully resetting the player. Menu deletion has confirmation to avoid accidental clicks.
