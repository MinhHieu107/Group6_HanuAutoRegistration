package com.hanu.registration.controller;

import com.hanu.registration.model.GlobalQueueEntry;
import com.hanu.registration.service.GlobalQueueStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DebugController {

    private final GlobalQueueStore globalQueueStore;

    public DebugController(GlobalQueueStore globalQueueStore) {
        this.globalQueueStore = globalQueueStore;
    }

    @GetMapping("/debug/queue")
    public List<GlobalQueueEntry> queue() {
        return globalQueueStore.getAllEntries();
    }
}