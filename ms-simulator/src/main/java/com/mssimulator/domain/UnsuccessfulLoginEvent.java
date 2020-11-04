package com.mssimulator.domain;

import org.kie.api.definition.type.Expires;
import org.kie.api.definition.type.Role;
import org.kie.api.definition.type.Timestamp;

import java.io.Serializable;
import java.util.Date;

@Role(Role.Type.EVENT)
@Timestamp("executionTime")
@Expires("1h")
public class UnsuccessfulLoginEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private Date executionTime;
    private String username;
    private String ip;
    private Boolean usernameRuleTriggered;
    private Boolean ipRuleTriggered;

    public Boolean getUsernameRuleTriggered() {
        return usernameRuleTriggered;
    }

    public void setUsernameRuleTriggered(Boolean usernameRuleTriggered) {
        this.usernameRuleTriggered = usernameRuleTriggered;
    }

    public Boolean getIpRuleTriggered() {
        return ipRuleTriggered;
    }

    public void setIpRuleTriggered(Boolean ipRuleTriggered) {
        this.ipRuleTriggered = ipRuleTriggered;
    }


    public UnsuccessfulLoginEvent() {
        this.executionTime = new Date();
        this.usernameRuleTriggered = false;
        this.ipRuleTriggered = false;
    }

    public UnsuccessfulLoginEvent(String username) {
        this.username = username;
        this.executionTime = new Date();
        this.usernameRuleTriggered = false;
        this.ipRuleTriggered = false;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
