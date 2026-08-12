package com.r3dpr0xy.charactersystem.prompt;

import java.util.UUID;

public final class PromptSession {
    private final UUID ownerId;
    private final UUID characterId;
    private PromptType type;
    private String name;
    private String surname = "";
    private Integer age;
    private Integer height;
    private String gender = "";

    public PromptSession(UUID ownerId, UUID characterId, PromptType type) {
        this.ownerId = ownerId;
        this.characterId = characterId;
        this.type = type;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public UUID characterId() {
        return characterId;
    }

    public PromptType type() {
        return type;
    }

    public void type(PromptType type) {
        this.type = type;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public Integer age() {
        return age;
    }

    public void age(Integer age) {
        this.age = age;
    }

    public String surname() {
        return surname;
    }

    public void surname(String surname) {
        this.surname = surname;
    }

    public Integer height() {
        return height;
    }

    public void height(Integer height) {
        this.height = height;
    }

    public String gender() {
        return gender;
    }

    public void gender(String gender) {
        this.gender = gender;
    }
}
