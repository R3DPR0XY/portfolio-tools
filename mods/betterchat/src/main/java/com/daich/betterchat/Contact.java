package com.daich.betterchat;

public class Contact {
    public String name;
    public String alias;

    public Contact() {
    }

    public Contact(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String label() {
        if (alias == null || alias.isBlank() || alias.equalsIgnoreCase(name)) {
            return name;
        }
        return alias + " (" + name + ")";
    }
}
