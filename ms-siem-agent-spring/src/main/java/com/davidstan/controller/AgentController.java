package com.davidstan.controller;

import com.davidstan.socket.SSLClientSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
public class AgentController {
    @PutMapping(path = "/setBatchValue/{batchValue}")
    public ResponseEntity<?> setBatch(@PathVariable int batchValue) {
        SSLClientSocketService.batchValue = batchValue;
        return ResponseEntity.ok(batchValue);
    }
}
