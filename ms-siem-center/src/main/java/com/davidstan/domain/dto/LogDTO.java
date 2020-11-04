package com.davidstan.domain.dto;

import com.davidstan.domain.LogType;

public class LogDTO {
    private String timestamp;
    private String message;
    private LogType type;
    private String sourceName;

    public LogDTO(String timestamp, String message, LogType type, String sourceName) {
        this.timestamp = timestamp;
        this.message = message;
        this.type = type;
        this.sourceName = sourceName;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public LogDTO(String timestamp, String message, LogType type) {
        this.timestamp = timestamp;
        this.message = message;
        this.type = type;
    }

    public LogType getType() {
        return type;
    }

    public void setType(LogType type) {
        this.type = type;
    }

    public LogDTO(String timestamp, String message) {
        this.timestamp = timestamp;
        this.message = message;
    }

    public LogDTO() {
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

    @Override
    public String toString() {
        return timestamp + " " + type + " "  + sourceName + " " + message;
    }
}
