package edu.java;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
public class LinkUpdaterScheduler {
    @Scheduled
    public void update() {
        System.out.println("Update");
    }
}
