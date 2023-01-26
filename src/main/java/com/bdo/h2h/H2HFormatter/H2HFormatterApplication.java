package com.bdo.h2h.H2HFormatter;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class H2HFormatterApplication implements CommandLineRunner{
	
	private boolean shouldRun = true;
    private WatchService watchService;
    @Value("${monitor.dir}")
    private String dirPath;
    
    @Override
    public void run(String... args) throws Exception {
    	try {
            Path dir = Paths.get(dirPath);
            watchService = FileSystems.getDefault().newWatchService();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.println("Watch service started and directory is registered");
        } catch (IOException e) {
            System.err.println("Error initializing watch service: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 1000)
    public void listenForNewFiles() throws InterruptedException {
    	if(!shouldRun) return; // Use this to stop listening
        if(watchService == null){
            System.err.println("watchService is not initialized, stopping listening for new files.");
            return;
        }
        try {
            WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("New file found: " + event.context().toString());
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
            System.out.println("Watch service closed, stopping listening for new files.");
        }
    }

	public static void main(String[] args) {
		SpringApplication.run(H2HFormatterApplication.class, args);
		System.out.println("H2H File Formatter Started.");
	}

}
