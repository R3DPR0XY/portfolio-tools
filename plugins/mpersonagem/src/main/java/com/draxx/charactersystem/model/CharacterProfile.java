package com.draxx.charactersystem.model;

import java.util.UUID;

public final class CharacterProfile {
    private final UUID id;
    private String name;
    private String surname;
    private String talent;
    private int age;
    private int height;
    private String gender;
    private String description;
    private long createdAt;

    public CharacterProfile(UUID id, String name, int age, int height, long createdAt) {
        this(id, name, "", "Sobrevivente", age, height, "", "", createdAt);
    }

    public CharacterProfile(UUID id, String name, String surname, int age, int height, String gender, String description, long createdAt) {
        this(id, name, surname, "Sobrevivente", age, height, gender, description, createdAt);
    }

    public CharacterProfile(UUID id, String name, String surname, String talent, int age, int height, String gender, String description, long createdAt) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.talent = talent;
        this.age = age;
        this.height = height;
        this.gender = gender;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String surname() {
        return surname;
    }

    public void surname(String surname) {
        this.surname = surname;
    }

    public String fullName() {
        return surname == null || surname.isBlank() ? name : name + " " + surname;
    }

    public String talent() {
        return talent;
    }

    public void talent(String talent) {
        this.talent = talent;
    }

    public int age() {
        return age;
    }

    public void age(int age) {
        this.age = age;
    }

    public int height() {
        return height;
    }

    public void height(int height) {
        this.height = height;
    }

    public String gender() {
        return gender;
    }

    public void gender(String gender) {
        this.gender = gender;
    }

    public String description() {
        return description;
    }

    public void description(String description) {
        this.description = description;
    }

    public long createdAt() {
        return createdAt;
    }

    public void createdAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
