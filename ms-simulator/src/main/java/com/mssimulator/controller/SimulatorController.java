package com.mssimulator.controller;

import com.mssimulator.service.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulatorController {
    @Autowired
    private SimulatorService simulatorService;

    @GetMapping(path = "/runSimulator")
    public void runSimulator() throws Exception {
        this.simulatorService.executeSimulator();
    }
}
