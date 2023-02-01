package com.bdo.h2h.H2HFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.bdo.h2h.H2HFormatter.filemonitor.EncryptWatchServiceSingleton;
import com.bdo.h2h.H2HFormatter.filemonitor.InputWatchServiceSingleton;

@SpringBootApplication
@EnableScheduling
public class H2HFormatterApplication implements CommandLineRunner{
	
	private boolean shouldRun = true;
    private WatchService inputWatchService;
    private WatchService encryptWatchService;
    
    @Value("${gpg.key.dir}")
    private String gpgKeyDir;
    @Value("${gpg.key.public}")
    private String publicKey;
    @Value("${gpg.key.private}")
    private String privateKey;
    @Value("${gpg.recipient")
    private String recipient;
    
    @Value("${input.dir}")
    private String inputDir;
    @Value("${backup.dir}")
    private String backupDir;
    @Value("${decrypted.dir}")
    private String decryptedDir;
    @Value("${processed.dir}")
    private String processedDir;
    @Value("${output.dir}")
    private String outputDir;
    
    private String separator = File.separator;
    
    @Override
    public void run(String... args) throws Exception {
    }

    @Scheduled(fixedRate = 1000)
    public void listenForNewFiles() throws InterruptedException {
    	if(!shouldRun) return; // Use this to stop listening
        if(inputWatchService == null){
        	try {
                Path dir = Paths.get(inputDir);
                inputWatchService = InputWatchServiceSingleton.getInstance();
                dir.register(inputWatchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Input Watch service started and directory is registered");
                importPrivateKey();
            } catch (IOException e) {
                System.err.println("Error initializing watch service: " + e.getMessage());
            }
        }
        try {
            WatchKey key = inputWatchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path inputFileDir = (Path)key.watchable();
//                        System.out.println("Directory being watched: " + inputFileDir);
                        
                        Path filePath = inputFileDir.resolve((Path) event.context());
                        
                        String file = filePath.getFileName().toString();
                        System.out.println("New File found: " + file);
                        
//                        processFile(filePath);
                        backupFile(filePath);
                    	
                    	decryptFile(filePath);
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
            System.out.println("Watch service closed, stopping listening for new files.");
        }
    }
    
    @Scheduled(fixedRate = 1000)
    public void listenForProcessedFiles() throws InterruptedException {
    	if(!shouldRun) return; // Use this to stop listening
        if(encryptWatchService == null){
        	try {
                Path dir = Paths.get(decryptedDir);
                encryptWatchService = EncryptWatchServiceSingleton.getInstance();
                dir.register(encryptWatchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Processed Files Watch service started and directory is registered");
                importPublicKey();
            } catch (IOException e) {
                System.err.println("Error initializing watch service: " + e.getMessage());
            }
        }
        try {
            WatchKey key = encryptWatchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path decryptedDir = (Path)key.watchable();
//                        System.out.println("Directory being watched: " + inputFileDir);
                        
                        Path filePath = decryptedDir.resolve((Path) event.context());
                        
                        String file = filePath.getFileName().toString();
                        System.out.println("New File found: " + file);
                        
                        encryptFile(filePath);
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
            System.out.println("Watch service closed, stopping listening for new files.");
        }
    }
    
    public void importPrivateKey() {
    	System.out.println("Import GPG Private Key");
		String importPrivateKey = "gpg --import " + gpgKeyDir + separator + privateKey;
		
		try {
			Process importPrivateKeyProcess = Runtime.getRuntime().exec(importPrivateKey);
			
			importPrivateKeyProcess.waitFor();
			if (importPrivateKeyProcess.exitValue() == 0) {
			    System.out.println("Private Key Imported.");
			} else {
			    System.out.println("Private Key Import Failed.");
			    System.out.println("Terminate H2H File Formatter.");
			    System.exit(1);
			}

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public void importPublicKey() {
    	System.out.println("Import GPG Public Key");
    	String importPublicKey = "gpg --import " + gpgKeyDir + separator + publicKey;
		
		try {
			Process importPublicKeyProcess = Runtime.getRuntime().exec(importPublicKey);
			
			importPublicKeyProcess.waitFor();
			if (importPublicKeyProcess.exitValue() == 0) {
			    System.out.println("Public Key Imported.");
			} else {
			    System.out.println("Public Key Import Failed.");
			    System.out.println("Terminate H2H File Formatter.");
			    System.exit(1);
			}

		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
//    private void processFile(Path file) {
//    	backupFile(file);
//    	
//    	decryptFile(file);
//    	
//    }

	public void backupFile(Path file) {
		Path backupPath = Paths.get(backupDir + separator + file.getFileName().toString()); 
    	try {
			Files.copy(file, backupPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println(file.getFileName().toString() + " backup failed.");
			e.printStackTrace();
		}
	}
	
	public void decryptFile(Path file) {
		String decryptedFileName = file.getFileName().toString().replace(".gpg", "");
        String decryptedFilePath = decryptedDir + separator + decryptedFileName;
		
        String command = "gpg --decrypt --output \"" + decryptedFilePath + "\" \"" + file.toAbsolutePath() + "\"";
        
		Process decryptFile;
		try {
			decryptFile = Runtime.getRuntime().exec(command);
			
			// Redirect standard error output to a stream
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(decryptFile.getErrorStream()));

			StringBuilder errorMessage = new StringBuilder();
			String line;
			while ((line = errorReader.readLine()) != null) {
			    errorMessage.append(line).append("\n");
			}

			// Wait for the process to complete
			decryptFile.waitFor();

			if (decryptFile.exitValue() == 0) {
				System.out.println("Decryption successful");
			} else {
			    System.out.println("Decryption failed with exit code " + decryptFile.exitValue() + " and error message:\n" + errorMessage);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void encryptFile(Path file) {
		String suffix = ".gpg";
		String encryptedFileName = file.getFileName().toString() + suffix;
        String encryptedFilePath = outputDir + separator + encryptedFileName;
        recipient = "gpborla@gmail.com"; //temporary solultion
//		System.out.println("EMAIL: " + recipient);
        String command = "gpg --trust-model always --encrypt -r " + recipient + " --output \"" + encryptedFilePath + "\" \"" + file.toAbsolutePath() + "\"";
        System.out.println("encryption: " + command);
		Process encryptFile;
		try {
			encryptFile = Runtime.getRuntime().exec(command);
			
			// Redirect standard error output to a stream
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(encryptFile.getErrorStream()));

			StringBuilder errorMessage = new StringBuilder();
			String line;
			while ((line = errorReader.readLine()) != null) {
			    errorMessage.append(line).append("\n");
			}

			// Wait for the process to complete
			encryptFile.waitFor();

			if (encryptFile.exitValue() == 0) {
				System.out.println("Encryption successful");
			} else {
				System.out.println("Encryption failed with exit code " + encryptFile.exitValue() + " and error message:\n" + errorMessage);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(H2HFormatterApplication.class, args);
		System.out.println("H2H File Formatter Started.");
	}

}
