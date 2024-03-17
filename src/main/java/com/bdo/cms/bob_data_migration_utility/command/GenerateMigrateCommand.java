package com.bdo.cms.bob_data_migration_utility.command;

import com.bdo.cms.bob_data_migration_utility.exception.DmServiceException;
import com.bdo.cms.bob_data_migration_utility.service.DmService;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;


@Getter
@ToString
@Slf4j
@Component
@CommandLine.Command(name = "genemigrate",
        description = "Generate and migrate.")
public class GenerateMigrateCommand implements Runnable {

    @Autowired
    DmService dmService;

    @Override
    public void run() {
        log.info("OPTION: GENERATE & MIGRATE - query, transform, save to DB, write to csv and migrate to raw tables.");
        try {
            dmService.generate();            
            dmService.migrate();
        } catch (DmServiceException e) {
            
            if(e.getCause().getCause().getClass().toString().equals("class java.io.FileNotFoundException")) {
                
                log.error("ERROR: (genemigrate): {}", e.getMessage());
            }
            else {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.error("ERROR: (genemigrate): {}", sw.toString());
                throw new RuntimeException();
            }

        }
    }
}
