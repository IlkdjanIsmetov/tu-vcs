package com.ksig.tu_vcs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class TuVcsApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodRuns() {
        assertDoesNotThrow(() ->
                TuVcsApplication.main(new String[]{})
        );
    }
}