/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bdo.cms.bob_data_migration_utility.command;

import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.exception.QueriesServiceException;
import com.bdo.cms.bob_data_migration_utility.service.queries.QueriesService;
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

/**
 *
 * @author a025012567
 */
@Service
@Getter
@ToString
@Slf4j
@Component
@CommandLine.Command(name = "dm", description = "Command to truncate tables in CIBUT database")
public class TruncateCommand implements Runnable {
    
    @Autowired
    QueriesService queriesService;
    
    @Autowired
    BobDatabaseConfig bobcfg;
    
    @Override
    public void run() {
        
        try {
            
            try (Connection connection = bobcfg.cibutDb().getConnection()) {
                queriesService.truncateTempTables(connection);
                connection.close();
            }
            
        } catch (QueriesServiceException | SQLException ex) {
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            log.error("ERROR: (truncate): {}", sw.toString());
            
        }
    
    }
    
}
