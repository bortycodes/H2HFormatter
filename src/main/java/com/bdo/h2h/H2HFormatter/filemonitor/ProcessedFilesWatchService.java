package com.bdo.h2h.H2HFormatter.filemonitor;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import org.springframework.stereotype.Component;

@Component
public class ProcessedFilesWatchService {
	private static WatchService watchService;
	
	private ProcessedFilesWatchService() {}
	
	public static WatchService getInstance() {
		if (watchService == null) {
			try {
				watchService = FileSystems.getDefault().newWatchService();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error initializing processed files watch service", e);
			}
		}
		return watchService;
	}
}
