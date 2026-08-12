package com.r3dpr0xy.charactersystem.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PlayerCharacters {
    private UUID activeCharacterId;
    private final List<CharacterProfile> characters = new ArrayList<>();

    public UUID activeCharacterId() {
        return activeCharacterId;
    }

    public void activeCharacterId(UUID activeCharacterId) {
        this.activeCharacterId = activeCharacterId;
    }

    public List<CharacterProfile> characters() {
        return characters;
    }

    public Optional<CharacterProfile> activeCharacter() {
        if (activeCharacterId == null) {
            return Optional.empty();
        }
        return character(activeCharacterId);
    }

    public Optional<CharacterProfile> character(UUID id) {
        return characters.stream().filter(character -> character.id().equals(id)).findFirst();
    }
}
