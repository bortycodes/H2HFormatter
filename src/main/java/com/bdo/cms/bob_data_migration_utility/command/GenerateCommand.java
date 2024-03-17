package com.bdo.cms.bob_data_migration_utility.command;

import com.bdo.cms.bob_data_migration_utility.exception.DmServiceException;
import com.bdo.cms.bob_data_migration_utility.service.DmService;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;


@Getter
@ToString
@Slf4j
@Component
@CommandLine.Command(name = "generate",
        description = "Querying data from BOB, transforming and saving them to CMS tables, and writing records to text files.")
public class GenerateCommand implements Runnable {

    @Autowired
    DmService dmService;

    @Override
    public void run() {
        log.info("OPTION: GENERATE - query, transform, save to DB, write to csv");
        try {
            dmService.generate();
        } catch (DmServiceException e) {
            
            if(e.getCause().getCause().getClass().toString().equals("class java.io.FileNotFoundException")) {
                
                log.error("ERROR: (generate): {}", e.getMessage());
            }
            else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.error("ERROR: (generate): {}", sw.toString());
                throw new RuntimeException();
            }

        }
    }
}
