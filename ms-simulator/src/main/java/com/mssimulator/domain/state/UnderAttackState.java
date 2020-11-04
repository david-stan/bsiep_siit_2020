package com.mssimulator.domain.state;


import com.mssimulator.domain.UnsuccessfulLoginEvent;

public class UnderAttackState extends State {

    public UnderAttackState(UnsuccessfulLoginEvent unsuccessfulLoginEvent) {
        super(unsuccessfulLoginEvent);
    }

    public UnderAttackState() {
        super();
    }
}
