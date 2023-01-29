package com.bdo.h2h.H2HFormatter.filemonitor;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import org.springframework.stereotype.Component;

@Component
public class WatchServiceSingleton {
	private static WatchService watchService;
	
	private WatchServiceSingleton () {}
	
	public static WatchService getInstance() {
		if (watchService == null) {
			try {
				watchService = FileSystems.getDefault().newWatchService();
			} catch (IOException e) {
				throw new RuntimeException("Error initializing watch service", e);
			}
		}
		return watchService;
	}
}
