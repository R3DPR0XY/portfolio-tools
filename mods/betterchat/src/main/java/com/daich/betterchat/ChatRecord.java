package com.daich.betterchat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ChatRecord {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    public String contactName;
    public String message;
    public boolean outgoing;
    public long timestamp;

    public ChatRecord() {
    }

    public ChatRecord(String contactName, String message, boolean outgoing) {
        this.contactName = contactName;
        this.message = message;
        this.outgoing = outgoing;
        this.timestamp = System.currentTimeMillis();
    }

    public String displayLine() {
        String direction = outgoing ? "Você" : contactName;
        return "[" + TIME_FORMAT.format(Instant.ofEpochMilli(timestamp)) + "] " + direction + ": " + message;
    }
}
