package com.bdo.cms.bob_data_migration_utility.command;

import com.bdo.cms.bob_data_migration_utility.exception.DmServiceException;
import com.bdo.cms.bob_data_migration_utility.service.DmService;
import java.io.PrintWriter;
import java.io.StringWriter;
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
@CommandLine.Command(name = "migrate",
        description = "Migrate records from CMS Temp Tables in BOB to CMS Staging Tables in CMS")
public class MigrateCommand implements Runnable {

    @Autowired
    DmService dmService;

    @Override
    public void run() {
        log.info("OPTION: MIGRATE - migrate records from BOB to CMS");
        try {
            dmService.migrate();
        } catch (DmServiceException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.error("ERROR: (migrate): {}", sw.toString());
            throw new RuntimeException();
        }
    }
}
