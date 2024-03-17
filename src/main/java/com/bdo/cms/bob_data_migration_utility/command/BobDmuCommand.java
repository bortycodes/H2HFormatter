package com.bdo.cms.bob_data_migration_utility.command;

import com.bdo.cms.bob_data_migration_utility.config.AppConfig;
import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.CmsDatabaseConfig;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import picocli.CommandLine;

@Service
@Getter
@ToString
@Slf4j
@Component
@CommandLine.Command(name = "dm", description = "Command to test BOB DMU")
public class BobDmuCommand implements Runnable {

    @Autowired
    AppConfig appCfg;
    
    @Autowired
    BobDatabaseConfig bobCfg;
    
    @Autowired
    CmsDatabaseConfig cmsDbCfg;
    
    @Override
    public void run() {
        log.info("OPTION: NONE - Test command");
        try {
                        
            Connection cibdb_connection = bobCfg.cibDb().getConnection();
            log.info("Successfully connected to BOB SOURCE DB.");
            cibdb_connection.close();
            
            Connection cibutdb_connection = bobCfg.cibutDb().getConnection();
            log.info("Successfully connected to BOB TEMPORARY DB.");
            cibutdb_connection.close();
            
            Connection cmsdb = cmsDbCfg.cmsDb().getConnection();
            log.info("Successfully connected to CMS DB.");
            cmsdb.close();
            
        } catch (SQLException ex) {
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            log.error("ERROR: (test-command): {}", sw.toString());
            
        }
    }
}
