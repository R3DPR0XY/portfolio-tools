package com.draxx.charactersystem.menu;

import java.util.UUID;

public record MenuAction(MenuActionType type, UUID characterId, String value) {
    public MenuAction(MenuActionType type, UUID characterId) {
        this(type, characterId, "");
    }
}
