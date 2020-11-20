package com.fbdev.helios.model;

/**
 * Federico Berti
 * <p>
 * Copyright 2020
 */
public interface IoProvider {
    int readSoundData(int address);

    boolean isSoundEnabled();

    boolean isIntEnabled();
}
