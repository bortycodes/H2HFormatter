package com.bdo.h2h.H2HFormatter.filemonitor;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import org.springframework.stereotype.Component;

@Component
public class InputWatchServiceSingleton {
	private static WatchService watchService;
	
	private InputWatchServiceSingleton () {}
	
	public static WatchService getInstance() {
		if (watchService == null) {
			try {
				watchService = FileSystems.getDefault().newWatchService();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Error initializing input watch service", e);
			}
		}
		return watchService;
	}
}
