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
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bdo.h2h.H2HFormatter.filemonitor.DecryptedFilesWatchService;
import com.bdo.h2h.H2HFormatter.filemonitor.InputFilesWatchService;
import com.bdo.h2h.H2HFormatter.filemonitor.ProcessedFilesWatchService;

@SpringBootApplication
@EnableScheduling
public class H2HFormatterApplication implements CommandLineRunner{
	
	private static final Logger LOG = LogManager.getLogger(H2HFormatterApplication.class);
	
	private boolean shouldRun = true;
    private WatchService inputFilesWatchService;
    private WatchService processedFilesWatchService;
    private WatchService decryptedFilesWatchService;
    
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
        if(inputFilesWatchService == null){
        	try {
                Path dir = Paths.get(inputDir);
                inputFilesWatchService = InputFilesWatchService.getInstance();
                dir.register(inputFilesWatchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Watch service for input files started.");
                System.out.println("Listening for input files in " + inputDir);
                importPrivateKey();
            } catch (IOException e) {
                System.err.println("Error initializing Watch Service for input files: " + e.getMessage());
                System.out.println("Terminate H2H File Formatter.");
			    System.exit(1);
            }
        }
        try {
            WatchKey key = inputFilesWatchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path inputFileDir = (Path)key.watchable();
                        
                        Path filePath = inputFileDir.resolve((Path) event.context());
                        
                        String file = filePath.getFileName().toString();
                        
                        if (file.equalsIgnoreCase("shutdown-h2h.txt")) {
                        		LOG.info("Shutdown file received.");
                        		LOG.info("H2H Formatter Terminated.");
                        		System.exit(0);
                        }
                        
                        LOG.info("New File found: " + file);
                        
                        String fileType = null;
                        int dotIndex = file.lastIndexOf(".");
                        if (dotIndex != -1) {
                            fileType = file.substring(dotIndex + 1);
                        }
                        
                        if ("gpg".equals(fileType))
                        	backupFile(filePath);
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
            System.out.println("Watch Service for input files closed, stopping listening for input files.");
        }
    }

    @Scheduled(fixedRate = 1000)
    public void listenForDecryptedFiles() throws InterruptedException {
    	if(!shouldRun) return; // Use this to stop listening
        if(decryptedFilesWatchService == null){
        	try {
                Path dir = Paths.get(decryptedDir);
                decryptedFilesWatchService = DecryptedFilesWatchService.getInstance();
                dir.register(decryptedFilesWatchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Watch Service for decrypted files started.");
                System.out.println("Listening for decrypted files in " + decryptedDir);
            } catch (IOException e) {
                System.err.println("Error initializing Watch Service for decrypted files: " + e.getMessage());
                System.out.println("Terminate H2H File Formatter.");
			    System.exit(1);
            }
        }
        try {
            WatchKey key = decryptedFilesWatchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path decryptedDir = (Path)key.watchable();
                        
                        Path filePath = decryptedDir.resolve((Path) event.context());
                        
//                        String file = filePath.getFileName().toString();
//                        System.out.println("New File found: " + file);
                        System.out.println("Processing " + filePath.getFileName().toString() + "...");
                        
                        Path path = Paths.get(decryptedDir.toUri());
                        
                        //process file
                        Path newFile = (Path) event.context();
                        Path fullPath = path.resolve(newFile);
                        File file = fullPath.toFile();
                        // process the file
                        List<String> lines = Files.readAllLines(fullPath);
                        List<String> updatedLines = lines.stream()
                                .map(line -> {
                                    if (line.startsWith("D") && line.split("\\|").length < 24) {
                                        int difference = 25 - line.split("\\|").length;
                                        StringBuilder sb = new StringBuilder(line);
                                        for (int i = 0; i < difference; i++) {
                                            sb.append("|");
                                        }
                                        return sb.toString();
                                    }
                                    return line;
                                })
                                .collect(Collectors.toList());
                        Files.write(fullPath, updatedLines);
                        // move the file to the processed directory
                        Files.move(fullPath, Paths.get(processedDir, newFile.toString()));
                        System.out.println("Processed " + filePath.getFileName().toString());
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
        	e.printStackTrace();
            System.out.println("Watch Service for decrypted files closed, stopping listening for decrypted files.");
        } catch (IOException e) {
			System.out.println("Error in processing file.");
			e.printStackTrace();
		}
    }
    
    @Scheduled(fixedRate = 1000)
    public void listenForProcessedFiles() throws InterruptedException {
    	if(!shouldRun) return; // Use this to stop listening
        if(processedFilesWatchService == null){
        	try {
                Path dir = Paths.get(processedDir);
                processedFilesWatchService = ProcessedFilesWatchService.getInstance();
                dir.register(processedFilesWatchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("Watch service for processed files started.");
                System.out.println("Listening for processed files in " + processedDir);
                importPublicKey();
            } catch (IOException e) {
                System.err.println("Error initializing Watch Service for processed files: " + e.getMessage());
                System.out.println("Terminate H2H File Formatter.");
			    System.exit(1);
            }
        }
        try {
            WatchKey key = processedFilesWatchService.poll(1, TimeUnit.SECONDS);
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path processedDir = (Path)key.watchable();
//                        System.out.println("Directory being watched: " + inputFileDir);
                        
                        Path filePath = processedDir.resolve((Path) event.context());
                        
                        String file = filePath.getFileName().toString();
                        System.out.println("New File found: " + file);
                        
                        encryptFile(filePath);
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException e) {
            System.out.println("Watch Service for processed files closed, stopping listening for processed files.");
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

	public void backupFile(Path file) {
		Path backupPath = Paths.get(backupDir + separator + file.getFileName().toString()); 
    	try {
			Files.copy(file, backupPath, StandardCopyOption.REPLACE_EXISTING);
			decryptFile(file);
		} catch (IOException e) {
			System.out.println(file.getFileName().toString() + " backup failed.");
			e.printStackTrace();
			System.out.println("Terminate H2H File Formatter.");
		    System.exit(1);
		}
	}
	
	public void deleteFile(Path file) {
		try {
            Files.delete(file);
        } catch (IOException e) {
            System.out.println("Unable to delete " + file.getFileName().toString() + " " + e.getMessage());
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
				System.out.println("Deleting " + file.getFileName().toString());
				deleteFile(file);
			} else {
			    System.out.println("Decryption failed with exit code " + decryptFile.exitValue() + " and error message:\n" + errorMessage);
			    LOG.info("Decryption failed with exit code " + decryptFile.exitValue() + " and error message:\n" + errorMessage);
			    Path backUpDir = Paths.get(backupDir);
			    file = backUpDir.resolve(file.getFileName().toString());
			    deleteFile(file);
			    LOG.info("Deleted " + file.getFileName().toString() + " from " + file.getParent());
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
//        System.out.println("encryption: " + command);
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
				System.out.println("Deleting " + file.getFileName().toString());
				deleteFile(file);
			} else {
				System.out.println("Encryption failed with exit code " + encryptFile.exitValue() + " and error message:\n" + errorMessage);
				System.out.println("Terminate H2H File Formatter.");
			    System.exit(1);
			}
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(H2HFormatterApplication.class, args);
		System.out.println("H2H File Formatter Started.");
		LOG.info("H2H File Formatter Started.");
	}

}
