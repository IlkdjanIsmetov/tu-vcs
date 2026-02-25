package com.ksig.tu_vcs.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ExampleController {

    @GetMapping("/example")
    public ResponseEntity<String> example() {
        return ResponseEntity.ok("this is an example");
    }
}
