package edu.java.services.interfaces;

import java.time.Duration;

public interface LinkUpdater {
    int update(Duration updateInterval);
}
