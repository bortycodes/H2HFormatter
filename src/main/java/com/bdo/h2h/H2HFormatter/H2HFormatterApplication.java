package com.bdo.h2h.H2HFormatter;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

import com.bdo.h2h.H2HFormatter.filemonitor.WatchServiceSingleton;

@SpringBootApplication
@EnableScheduling
public class H2HFormatterApplication implements CommandLineRunner{
	
	private boolean shouldRun = true;
    private WatchService watchService;
    
    @Value("${input.dir}")
    private String inputDir;
    @Value("${backup.dir}")
    private String backupDir;
    
    @Override
    public void run(String... args) throws Exception {
    }

    @Scheduled(fixedRate = 1000)
    public void listenForNewFiles() throws InterruptedException {
    	if(!shouldRun) return; // Use this to stop listening
        if(watchService == null){
        	try {
                Path dir = Paths.get(inputDir);
                watchService = WatchServiceSingleton.getInstance();
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Watch service started and directory is registered");
            } catch (IOException e) {
                System.err.println("Error initializing watch service: " + e.getMessage());
            }
        }
        try {
            WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        //System.out.println("New file found: " + event.context().toString());
                        String file= event.context().toString();
                        System.out.println("New File found: " + file);
                        processFile(file);
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
            System.out.println("Watch service closed, stopping listening for new files.");
        }
    }
    
    private void processFile(String file) {
    	Path originalPath = Paths.get(inputDir + "/" + file);
    	Path backupPath = Paths.get(backupDir + "/" + file);
    	try {
			Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println(inputDir + "/" + file + " backup failed.");
			e.printStackTrace();
		}

    }
    
	public static void main(String[] args) {
		SpringApplication.run(H2HFormatterApplication.class, args);
		System.out.println("H2H File Formatter Started.");
	}

}
