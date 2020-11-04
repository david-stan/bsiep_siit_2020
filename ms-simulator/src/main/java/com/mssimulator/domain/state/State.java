package com.mssimulator.domain.state;


import com.mssimulator.domain.UnsuccessfulLoginEvent;

public abstract class State {
    private String name;
    UnsuccessfulLoginEvent unsuccessfulLoginEvent;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UnsuccessfulLoginEvent getUnsuccessfulLoginEvent() {
        return unsuccessfulLoginEvent;
    }

    public void setUnsuccessfulLoginEvent(UnsuccessfulLoginEvent unsuccessfulLoginEvent) {
        this.unsuccessfulLoginEvent = unsuccessfulLoginEvent;
    }

    public State() {
    }

    public State(UnsuccessfulLoginEvent unsuccessfulLoginEvent) {
        this.unsuccessfulLoginEvent = unsuccessfulLoginEvent;
    }
}
