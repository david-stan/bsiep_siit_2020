package com.mssimulator.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    private LogType type;
    private String timestamp;
    private String message;
    private Boolean errorLogRuleTriggered;
    private String sourceName;

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Boolean getErrorLogRuleTriggered() {
        return errorLogRuleTriggered;
    }

    public void setErrorLogRuleTriggered(Boolean errorLogRuleTriggered) {
        this.errorLogRuleTriggered = errorLogRuleTriggered;
    }

    public Log(LogType type, String timestamp, String message, Boolean errorLogRuleTriggered) {
        this.type = type;
        this.timestamp = timestamp;
        this.message = message;
        this.errorLogRuleTriggered = errorLogRuleTriggered;
    }

    public Log(LogType type, String message) {
        this.type = type;
        this.message = message;
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
    }

    public Log() {
        this.timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        this.setErrorLogRuleTriggered(false);
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

    @Override
    public String toString() {
        return timestamp + " " + type + " " + sourceName + " " + message;
    }
}
