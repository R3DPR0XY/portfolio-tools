package com.r3dpr0xy.charactersystem.menu;

public final class CreationDraft {
    private String talent = "Sobrevivente";
    private int height;
    private String name = "";
    private String surname = "";
    private int age = 18;
    private String gender = "";
    private String description = "";

    public CreationDraft(int height) {
        this.height = height;
    }

    public String talent() {
        return talent;
    }

    public void talent(String talent) {
        this.talent = talent;
    }

    public int height() {
        return height;
    }

    public void height(int height) {
        this.height = height;
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

    public int age() {
        return age;
    }

    public void age(int age) {
        this.age = age;
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
}
