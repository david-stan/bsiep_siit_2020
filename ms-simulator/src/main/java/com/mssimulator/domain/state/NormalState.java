package com.mssimulator.domain.state;


import com.mssimulator.domain.UnsuccessfulLoginEvent;

public class NormalState extends State {
    public NormalState(UnsuccessfulLoginEvent unsuccessfulLoginEvent) {
        super(unsuccessfulLoginEvent);
    }

    public NormalState() {
    }
}
