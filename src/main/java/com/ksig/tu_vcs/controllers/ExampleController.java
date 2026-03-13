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


    @GetMapping("/public/test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("this is should be public");
    }

    @GetMapping("/private/test")
    public ResponseEntity<String> privateTest() {
        return ResponseEntity.ok("this is should be private");
    }


}
