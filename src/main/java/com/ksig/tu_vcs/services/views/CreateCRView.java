package com.ksig.tu_vcs.services.views;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateCRView {
    private String tittle;
    private long baseRevisionNUmber;
    private String description;
}
