package com.ksig.tu_vcs.services.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalItemMetadata {
    private String path;
    private String checksum;
    private Long lastPulledRevisionNumber;
}