package com.davidstan.domain;

public class Log {
    private LogType type;
    private String timestamp;
    private String message;
    private String sourceName;

    public Log(LogType type, String timestamp, String message) {
        this.type = type;
        this.timestamp = timestamp;
        this.message = message;
    }

    public Log() {
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
