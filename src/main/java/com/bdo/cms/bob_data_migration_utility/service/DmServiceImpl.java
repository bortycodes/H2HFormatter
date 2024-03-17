package com.bdo.cms.bob_data_migration_utility.service;

import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.CmsDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.FieldsAndTablesConfig;
import com.bdo.cms.bob_data_migration_utility.config.FileConfig;
import com.bdo.cms.bob_data_migration_utility.config.QueriesConfig;

import static com.bdo.cms.bob_data_migration_utility.constant.Constants.*;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.ConvertException;
import com.bdo.cms.bob_data_migration_utility.exception.DmServiceException;
import com.bdo.cms.bob_data_migration_utility.exception.FileServiceException;
import com.bdo.cms.bob_data_migration_utility.exception.MigrateException;
import com.bdo.cms.bob_data_migration_utility.exception.QueriesServiceException;
import com.bdo.cms.bob_data_migration_utility.service.convert.UsergpCodeConvertService;
import com.bdo.cms.bob_data_migration_utility.service.file.FileService;
import com.bdo.cms.bob_data_migration_utility.service.generate.GenerateService;
import com.bdo.cms.bob_data_migration_utility.service.migrate.MigrateService;
import com.bdo.cms.bob_data_migration_utility.service.migrate.MigrateServiceImpl;
import com.bdo.cms.bob_data_migration_utility.service.queries.QueriesService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

@Slf4j
@Service
public class DmServiceImpl implements DmService {

    @Autowired
    QueriesService queriesService;

    @Autowired
    FieldsAndTablesConfig ftcfg;

    @Autowired
    FileService fileService;

    @Autowired
    GenerateService generateService;

    @Autowired
    MigrateService migrateService;

    @Autowired
    UsergpCodeConvertService ugpConvertService;
    
    @Autowired
    BobDatabaseConfig bobcfg;
    
    @Autowired
    CmsDatabaseConfig cmscfg;
    
    @Autowired
    FileConfig cfg;
    
    @Autowired
    QueriesConfig qcfg;
    
    private static Map<String, String> specialCharacters = new HashMap<>();

    /**
     * Get and parse Input file into list
     * Call all queries and process 1 table per thread
     * Save 26 table outputs in a file in pipe delimited text format
     */
    
    public static List<Input> checkedInputs;
    
    @Override
    public void generate() throws DmServiceException {
        try {
            
            Connection cib_connection = bobcfg.cibDb().getConnection();
            Connection cibut_connection = bobcfg.cibutDb().getConnection();
            
            loadSpeciaCharacters(cibut_connection);
            
            // Delete inputs with flag 2
            queriesService.deleteInvalidInput(cibut_connection);

            // Read input file to string list
            List<Input> uncheckedInputs = fileService.read();            

            if (!uncheckedInputs.isEmpty()) {
                // Remove duplicates
                
                checkedInputs = queriesService.removeDuplicates(uncheckedInputs, cibut_connection);
                
                //get unique corp_codes
                List<Input> unique_corp_inputs = new ArrayList<>();
                List<String> unique_corp_codes = new ArrayList<>();
                for(Input input : checkedInputs)
                {
                    if(!unique_corp_codes.contains(input.getCorpCd())) {
                        unique_corp_inputs.add(input);
                        unique_corp_codes.add(input.getCorpCd());
                    }
                }
                
                //get unique cifs
                List<Input> unique_cif_inputs = new ArrayList<>();
                List<String> unique_cifs = new ArrayList<>();
                for(Input input : checkedInputs)
                {
                    if(!unique_cifs.contains(input.getCif())) {
                        unique_cif_inputs.add(input);
                        unique_cifs.add(input.getCif());
                    }
                }

                // Update usergroup code conversion table                
                ugpConvertService.convert(checkedInputs, cib_connection, cibut_connection);
                
                
                Map<String, String> bob_users = new HashMap<>();
                Map<String, String[]> new_bob_users = new HashMap<>();

                String getBobUsersQuery = "select CONCAT(CONCAT(LOGIN_ID, '-'), ORG_ID) KEY, DOMAIN_ID VALUE from BOB_USERS";
                PreparedStatement ps = cibut_connection.prepareStatement(getBobUsersQuery);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    bob_users.put(rs.getString("KEY"), rs.getString("VALUE"));
                }
                rs.close();
                ps.close();
                cib_connection.close();
                cibut_connection.close();                

                Map<String, Integer> numberOfRecordsPerTable = new HashMap<>();                                
                

                if (!checkedInputs.isEmpty()) {
                    ExecutorService es = Executors.newCachedThreadPool();
                    List<Callable<Map<String, Integer>>> tableTasks = new ArrayList<>();                                        
                    
                    for (String tableName : ftcfg.tableNames) {
                        
                        Callable<Map<String, Integer>> tableTask = null;
                        switch(tableName) {
                            case OD_USERS_MB:     
                            case OD_USER_LIMITS_MB:
                            case ORBIIBS_NICKNAME:
                            case OD_USER_FUNCTION_MB:
                            case ROLES:
                                if(tableName.equalsIgnoreCase(OD_USERS_MB)) 
                                    tableTask = () -> generateService.generateUserTables(unique_corp_inputs, checkedInputs, bobcfg.cibDb().getConnection(), bobcfg.cibutDb().getConnection());                                
                                break;                                                                                               
                            case OD_CORPORATE_LIMITS_MB:
                            case OD_CORPORATE_FUNCTION_MB:
                            case OD_USERGROUP_MB:                            
                            case OD_USERGP_LIMITS_MB:                                
                            case RULES:
                                tableTask = () -> generateService.generatePerTable(tableName, unique_corp_inputs, bobcfg.cibDb().getConnection(), bobcfg.cibutDb().getConnection());
                                break;
                            case CIM_CUST_DEFN_MB:
                            case CIM_CUST_CONTACT_INFO_MB:
                            case CIM_CUST_ACCT_MB:
                            case CIM_CUST_BILLING_DETAILS_MB:
                            case CIM_PAYMENT_BKFT_DT_MB:
                            case CIM_PAYMENT_CUST_PREF_MB:
                            case CIM_PAYMENT_PARAMS_MB:
                            case CIM_BUSINESS_PARAMS_MB:
                            case CIM_CUSTOMER_LIMIT_MB:
                            case CIM_SUBPROD_ATTR_MAP_MB:
                            case OD_USERGP_FUNCTION_MB:
                                tableTask = () -> generateService.generatePerTable(tableName, unique_cif_inputs, bobcfg.cibDb().getConnection(), bobcfg.cibutDb().getConnection());
                                break;
                            default:
                                tableTask = () -> generateService.generatePerTable(tableName, checkedInputs, bobcfg.cibDb().getConnection(), bobcfg.cibutDb().getConnection());
                                
                        }
                        
                        if(tableTask != null)
                            tableTasks.add(tableTask);
                    }

                    List<Future<Map<String, Integer>>> results = es.invokeAll(tableTasks);
                    es.shutdown();

                    for (Future<Map<String, Integer>> future : results) {
                        Map<String, Integer> result = future.get();
                        numberOfRecordsPerTable.putAll(result);
                    }

                    queriesService.saveProcessedInputs(checkedInputs);
                }

                Map<String, Integer> numberOfRecordsPerTable_sorted = new TreeMap<>(numberOfRecordsPerTable);

                int unchecked = uncheckedInputs.size();
                int checked = checkedInputs.size();
                fileService.createReport(
                        numberOfRecordsPerTable_sorted,
                        checked,
                        unchecked - checked,
                        "generate"
                );

                fileService.archive();
            }

        } catch (FileServiceException |
                InterruptedException |
                ExecutionException |
                QueriesServiceException |
                ConvertException | SQLException e) {
            throw new DmServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void migrate() throws DmServiceException {

        try {
        	
            Connection cmsConnection = cmscfg.cmsDb().getConnection(); // truncate CMS raw tables
            queriesService.truncateCMSrawTables(cmsConnection);
            cmsConnection.close();
            
            // get the processed inputs with flag = 2
            List<Input> processedInputs = queriesService.retrieveGenerated();

            Map<String, Integer> numberOfRecordsPerTable = new HashMap<>();

            if (!processedInputs.isEmpty()) {
                ExecutorService es = Executors.newCachedThreadPool();
                List<Callable<Map<String, Integer>>> tableTasks = new ArrayList<>();
                for (String tableName : ftcfg.tableNames) {
                    Callable<Map<String, Integer>> tableTask = () -> migrateService.migratePerTable(tableName, processedInputs);
                    tableTasks.add(tableTask);
                }

                List<Future<Map<String, Integer>>> results = es.invokeAll(tableTasks);
                es.shutdown();

                for (Future<Map<String, Integer>> future : results) {
                    Map<String, Integer> result = future.get();
                    numberOfRecordsPerTable.putAll(result);
                }
                
                if(MigrateServiceImpl.getTableMap("raw").get("28") != null)
                    insert_org_id_mapping();

                // update processed inputs
                queriesService.updateProcessedInputs(processedInputs);
                
                

            }
            
            Map<String, Integer> numberOfRecordsPerTable_sorted = new TreeMap<>(numberOfRecordsPerTable);

            fileService.createReport(
                    numberOfRecordsPerTable_sorted,
                    processedInputs.size(),
                    0,
                    "migrate"
            );                        

            // truncate temp tables
            Connection cibut_connection = bobcfg.cibutDb().getConnection();
            queriesService.truncateTempTables(cibut_connection);
            cibut_connection.close();


        } catch (QueriesServiceException |
                MigrateException |
                InterruptedException |
                ExecutionException |
                FileServiceException | SQLException e) {
            log.error("ERROR: (migrate): {}", e.getMessage());
            throw new DmServiceException(e.getMessage(), e);
        }
    }
        
    private void insert_org_id_mapping() throws DmServiceException{
        
        try {
            
            int record_count = 0;
            Map<String, String> org_id_map = new HashMap<>();
            Connection connection = cmscfg.cmsDb().getConnection();
            String query = "INSERT INTO RAW_28_ORG_DEF(ROW_DATA) VALUES(?)";
            PreparedStatement ps = connection.prepareStatement(query);
            
            File file = new File(cfg.inputPath + "org_id_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String corp_org;
                while ((corp_org = br.readLine()) != null) {

                    String[] details = corp_org.split("\\|");
                    
                    if (details.length > 1) {
                        if(details.length >= 3) {
                            if(org_id_map.get(details[1].trim()) == null) {
                                org_id_map.put(details[1].trim(), details[2].trim());


                                ps.setString(1, details[1].trim() + "|" + details[2].trim());
                                ps.executeUpdate();
                                record_count++;
                            }
                        } else {
                            log.error("Incomplete org details, please review the org mapping file.");
                        }
                    }

                }

            } catch (IOException e) {
                log.info("Org Mapping file not found!");
                throw new DmServiceException(e.getMessage(), e);
            } catch (SQLException ex) {
                log.error("ERROR: (migrate): {}", ex.getMessage());
                throw new DmServiceException(ex.getMessage(), ex);
            }
                
            ps.close();
            connection.close();
            
            MigrateServiceImpl.addTableRecordCount("ORG_DEF", record_count, record_count);
            
        } catch (SQLException | MigrateException ex) {
            log.error("ERROR: (migrate): {}", ex.getMessage());
            throw new DmServiceException(ex.getMessage(), ex);
        }
    }
    
    private void loadSpeciaCharacters(Connection bob_connection) throws DmServiceException {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
    	try {
    		
    		preparedStatement = bob_connection.prepareStatement(qcfg.specialCharactersMap);
    		resultSet = preparedStatement.executeQuery();
    		
    		while (resultSet.next()) {
    			String specialCharacter = resultSet.getString("character");
    			String replacement = resultSet.getString("replacement");
    			
    			specialCharacters.put(specialCharacter, replacement);
    		}
    		
    		log.info("*** SPECIAL CHARACTERS MAP SUCCESSFULLY POPULATED ***");
    		
    		for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
    			log.debug("KEY: {}, VALUE: {}", entry.getKey(), entry.getValue());
    		}
    		
    	} catch (SQLException e) {
            throw new DmServiceException(e.getMessage(), e);
        } finally {
        	if (resultSet != null)
				try {
					resultSet.close();
				} catch (SQLException e) {
					log.error("ERROR: Loading Special Characters. ResultSet closure failed: {}", e.getMessage());
				}
        	
        	if (preparedStatement != null)
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					log.error("ERROR: Loading Special Characters. PreparedStatement closure failed: {}", e.getMessage());
				}
        }
    	
    }
    
    public Map<String, String> getSpecialCharacters() {
    	return specialCharacters;
    }
}
