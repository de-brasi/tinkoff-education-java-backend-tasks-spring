package edu.java;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class LinkUpdaterScheduler {
    @Scheduled(fixedDelayString = "#{@scheduler.interval()}")
    public void update() {
        System.out.println("Update");
    }
}
