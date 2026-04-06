package com.ksig.tu_vcs.services.views;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorView {
    private String logId;
    private String message;
    private LocalDateTime timestamp;
}
