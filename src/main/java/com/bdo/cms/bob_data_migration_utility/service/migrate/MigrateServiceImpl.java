package com.bdo.cms.bob_data_migration_utility.service.migrate;

import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.CmsDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.MigrateException;
import com.bdo.cms.bob_data_migration_utility.exception.ResultSetParserException;
import com.bdo.cms.bob_data_migration_utility.service.parser.ResultSetParserService;
import com.bdo.cms.bob_data_migration_utility.service.queries.QueriesService;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@Service
@Slf4j
public class MigrateServiceImpl implements MigrateService {

    @Autowired
    CmsDatabaseConfig cmsDbCfg;

    @Autowired
    BobDatabaseConfig bobDbCfg;

    @Autowired
    QueriesService queriesService;

    @Autowired
    ResultSetParserService resultSetParserService;

    @Override
    public Map<String, Integer> migratePerTable(String tableName, List<Input> inputs) throws MigrateException {
        log.info("[{}] - MIGRATE process started", tableName);
        int numberOfRecords = 0;

        // for rules tables
        int rulesRecords = 0;
        int rulesDefRecords = 0;
        int parsedRuleRecords = 0;
        int rulesAccRecords = 0;

        // for roles tables
        int rolesRecords = 0;
        int userRolesRecords = 0;

        String query = queriesService.getMgrtSelectQuery(tableName);

            if (tableName.equals(RULES)) {
                String rulesQuery = queriesService.getMgrtSelectQuery(OD_RULES_MB);

                rulesRecords += migrateAll(OD_RULES_MB, rulesQuery);

                String rulesDefQuery = queriesService.getMgrtSelectQuery(OD_RULES_DEF_MB);

                rulesDefRecords += migrateAll(OD_RULES_DEF_MB, rulesDefQuery);

                String parsedRuleQuery = queriesService.getMgrtSelectQuery(OD_PARSED_RULE_MB);

                parsedRuleRecords += migrateAll(OD_PARSED_RULE_MB, parsedRuleQuery);

                String rulesAccQuery = queriesService.getMgrtSelectQuery(OD_RULES_ACC_MAP_MB);

                rulesAccRecords += migrateAll(OD_RULES_ACC_MAP_MB, rulesAccQuery);

            } else if (tableName.equals(ROLES)) {
                String rolesQuery = queriesService.getMgrtSelectQuery(OD_ROLES_MB);

                rolesRecords += migrateAll(OD_ROLES_MB, rolesQuery);

                String userRolesQuery = queriesService.getMgrtSelectQuery(OD_USER_ROLES_MAP_MB);

                userRolesRecords += migrateAll(OD_USER_ROLES_MAP_MB, userRolesQuery);

            } else {

                numberOfRecords += migrateAll(tableName, query);
            }

        Map<String, Integer> recordsPerTable = new HashMap<>();

        if (tableName.equals(RULES)) {
            recordsPerTable.put(OD_RULES_MB, rulesRecords);
            recordsPerTable.put(OD_RULES_DEF_MB, rulesDefRecords);
            recordsPerTable.put(OD_PARSED_RULE_MB, parsedRuleRecords);
            recordsPerTable.put(OD_RULES_ACC_MAP_MB, rulesAccRecords);

        } else if (tableName.equals(ROLES)) {
            recordsPerTable.put(OD_ROLES_MB, rolesRecords);
            recordsPerTable.put(OD_USER_ROLES_MAP_MB, userRolesRecords);

        } else {
            recordsPerTable.put(tableName, numberOfRecords);
        }

        log.info("[{}] - MIGRATE process ended", tableName);
        return recordsPerTable;
    }

    private int migrate(String tableName, Input input, String query) throws MigrateException {
        int recordsInserted = 0;
        try (
                Connection bobCon = bobDbCfg.cibutDb().getConnection();
                PreparedStatement bobPs = bobCon.prepareStatement(query);
                Connection cmsCon = cmsDbCfg.cmsDb().getConnection();
        ) {
            switch (tableName) {
                case CIM_CUST_DEFN_MB:
                case CIM_CUST_ACCT_MB:
                case CIM_CUST_CONTACT_INFO_MB:
                case CIM_CUSTOMER_LIMIT_MB:
                case BENEFICIARY_MAINTENANCE:
                    bobPs.setString(1, input.getCif());
                    break;
                default:
                    bobPs.setString(1, input.getCorpCd());
            }

            ResultSet rs = bobPs.executeQuery();
            List<Map<String, String>> rsmaplist = new ArrayList<>();
            int column_count = rs.getMetaData().getColumnCount();
            while(rs.next()){
                
                Map<String, String> rsmap = new HashMap<>();
                for(int x = 0; x < column_count; x++)
                {
                    String column_name = rs.getMetaData().getColumnName(x);
                    String value = rs.getString(column_name);
                    rsmap.put(column_name, value);
                }
                rsmaplist.add(rsmap);                
            }

            rs.close();
            bobPs.close();
            bobCon.close();
            
            List<String> rows = resultSetParserService.parseToString(rsmaplist, tableName);                                    
            
            if (!rows.isEmpty()) {
//                StringBuilder insertQry = new StringBuilder("INSERT ALL\
                PreparedStatement ps = cmsCon.prepareStatement(
                        "INSERT INTO " + getStagingTableName(tableName) + " (ROW_DATA) VALUES (?)"
                );
                int rowSize = rows.size();
                for (int i = 0; i < rowSize; i++) {
                    ps.setString(1, rows.get(i));
                    ps.addBatch();

                    if ((i + 1) % RECORDS_PER_BATCH == 0 || i == rowSize - 1) {
                        ps.executeBatch();
                        recordsInserted += ((i % RECORDS_PER_BATCH) + 1);
                        

                    }
//                    insertQry.append("INTO ").append(getStagingTableName(tableName)).append(" (ROW_DATA) VALUES (q'[")
//                            .append(row).append("]')\n");
                }
                ps.close();
                cmsCon.close();
                log.info("[{}] Inserted {} record/s.", tableName, rowSize);
//                insertQry.append("SELECT * FROM DUAL");

//            System.out.println(insertQry);
//                PreparedStatement cmsPs = cmsCon.prepareStatement(insertQry.toString());
//                recordsInserted = cmsPs.executeUpdate();
//                cmsPs.close();
            }



        } catch (SQLException | ResultSetParserException e) {
            log.error(e.getMessage(), e);
            throw new MigrateException(e.getMessage(), e);
        }


        return recordsInserted;
    }
    
    private static Map<String, String> temp_table_map;
    private static Map<String, String> raw_table_map;
    public static synchronized Map<String, String> getTableMap(String type) throws MigrateException {
        
        if(type.equalsIgnoreCase("temp")) {
            if(temp_table_map == null) {
                temp_table_map = new HashMap<>();
                File file = new File("config/tables.csv");
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {                   
                    String tables;
                    while ((tables = br.readLine()) != null) {

                        String[] details = tables.split("\\|");
                        if (details.length > 1) {
                            if(details.length >= 3) {                            
                                if(temp_table_map.get(details[1]) == null)
                                    temp_table_map.put(details[1], details[0]);
                            } else {
                                log.error("Incomplete table details, please review the table mapping file.");
                            }
                        }

                    }

                } catch (IOException e) {
                    log.info("Table Mapping file not found!");
                    throw new MigrateException(e.getMessage(), e);
                }
            }
            
            return temp_table_map;
            
        } else if (type.equalsIgnoreCase("raw")){
            
            if(raw_table_map == null) {
                raw_table_map = new HashMap<>();
                File file = new File("config/tables.csv");
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {                    
                    String tables;
                    while ((tables = br.readLine()) != null) {

                        String[] details = tables.split("\\|");
                        if (details.length > 1) {
                            if(details.length >= 3) {                            
                                if(raw_table_map.get(details[0]) == null)
                                    raw_table_map.put(details[0], details[2]);
                            } else {
                                log.error("Incomplete table details, please review the table mapping file.");
                            }
                        }

                    }

                } catch (IOException e) {
                    log.info("Table Mapping file not found!");
                    throw new MigrateException(e.getMessage(), e);
                }
            }
            return raw_table_map;
            
        } else {
            
            throw new MigrateException("Invalid table type.", new Exception());
            
        }
    }
    
    public static List<String> tableRecordCountList = new ArrayList<>();
    public static synchronized void addTableRecordCount(String tableName, int tempTableCount, int rawTableCount) throws MigrateException {
        
        
        String tbl_no = getTableMap("temp").get(tableName);
        if(tbl_no != null) {
            String raw_tbl = getTableMap("raw").get(tbl_no);
            if(raw_tbl != null) {
                String tbl_details = "";
                tbl_details += StringUtils.leftPad(tbl_no, 2, "0") + "\t";
                tbl_details += StringUtils.rightPad(tableName, 35, " ");
                tbl_details += StringUtils.rightPad(Integer.toString(tempTableCount), 20, " ") + "\t";
                tbl_details += StringUtils.rightPad(raw_tbl, 35, " ");
                tbl_details += StringUtils.rightPad(Integer.toString(rawTableCount), 20, " ");
                if(tempTableCount != rawTableCount)
                    tbl_details += "*with discrepancies";
                tableRecordCountList.add(tbl_details);
            }
        }        
        
    }
    
    private int migrateAll(String tableName, String query) throws MigrateException {
        int recordsInserted = 0;
        try (
                Connection bobCon = bobDbCfg.cibutDb().getConnection();
                PreparedStatement bobPs = bobCon.prepareStatement(query);
                Connection cmsCon = cmsDbCfg.cmsDb().getConnection();
        ) {
            
            ResultSet rs = bobPs.executeQuery();
            List<Map<String, String>> rsmaplist = new ArrayList<>();
            int column_count = rs.getMetaData().getColumnCount();
            while(rs.next()){
                
                Map<String, String> rsmap = new HashMap<>();
                for(int x = 1; x <= column_count; x++)
                {
                    String column_name = rs.getMetaData().getColumnName(x);
                    String value = rs.getString(column_name);
                    rsmap.put(column_name, value);
                }
                rsmaplist.add(rsmap);                
            }

            rs.close();
            bobPs.close();
            bobCon.close();
            
            List<String> rows = resultSetParserService.parseToString(rsmaplist, tableName);                        
            
            if (!rows.isEmpty()) {

                PreparedStatement ps = cmsCon.prepareStatement(
                        "INSERT INTO " + getStagingTableName(tableName) + " (ROW_DATA) VALUES (?)"
                );
                int rowSize = rows.size();
                for (int i = 0; i < rowSize; i++) {
                    ps.setString(1, rows.get(i));
                    ps.addBatch();

                    if ((i + 1) % RECORDS_PER_BATCH == 0 || i == rowSize - 1) {
                        ps.executeBatch();
                        recordsInserted += ((i % RECORDS_PER_BATCH) + 1);                        
                    }

                }
                ps.close();
                cmsCon.close();
                log.info("[{}] Inserted {} record/s.", tableName, rowSize);

            }
            
            addTableRecordCount(tableName, rsmaplist.size(), recordsInserted);

        } catch (SQLException | ResultSetParserException e) {
            log.error(tableName + " : " + e.getMessage(), e);
            throw new MigrateException(e.getMessage(), e);
        }


        return recordsInserted;
    }

    private String getStagingTableName(String tableName) {
        switch (tableName) {
            case CIM_CUST_DEFN_MB:
                return "RAW_01_CUST_DEFN";
            case CIM_CUST_CONTACT_INFO_MB:
                return "RAW_02_CUST_CONTACT_INFO";
            case CIM_CUST_ACCT_MB:
                return "RAW_03_CUST_ACCT";
            case CIM_CUST_BILLING_DETAILS_MB:
                return "RAW_04_CUST_BILLING_DETAILS";
            case CIM_PAYMENT_BKFT_DT_MB:
                return "RAW_05_PAYMENT_BKFT_DT";
            case CIM_PAYMENT_CUST_PREF_MB:
                return "RAW_06_PAYMENT_CUST_PREF";
            case CIM_PAYMENT_PARAMS_MB:
                return "RAW_07_PAYMENT_PARAMS";
            case CIM_BUSINESS_PARAMS_MB:     
                return "RAW_08_BUSINESS_PARAMS";
            case CIM_DOMAIN_DEFN:
                return "RAW_09_DOMAIN_DEFN";
            case CIM_CUSTOMER_LIMIT_MB:
                return "RAW_10_CUSTOMER_LIMIT";
            case OD_CORPORATE_LIMITS_MB:
                return "RAW_12_CORPORATE_LIMITS";
            case OD_CORPORATE_FUNCTION_MB:
                return "RAW_13_CORPORATE_FUNCTION";
            case OD_USERGROUP_MB:
                return "RAW_14_USERGROUP";
            case OD_USERGP_FUNCTION_MB:
                return "RAW_15_USERGP_FUNCTION";
            case OD_USERGP_LIMITS_MB:
                return "RAW_16_USERGP_LIMITS";
            case OD_USERS_MB:
                return "RAW_17_USERS";
            case OD_USER_LIMITS_MB:
                return "RAW_18_USER_LIMITS";
            case ORBIIBS_NICKNAME:
                return "RAW_19_ORBIIBS_NICKNAME";
            case OD_USER_FUNCTION_MB:
                return "RAW_20_USER_FUNCTION";
            case OD_ROLES_MB:
                return "RAW_21_ROLES";
            case OD_USER_ROLES_MAP_MB:
                return "RAW_22_USER_ROLES_MAP";
            case OD_RULES_MB:
                return "RAW_23_RULES";
            case OD_RULES_ACC_MAP_MB:
                return "RAW_24_RULES_ACC_MAP";
            case OD_RULES_DEF_MB:
                return "RAW_25_RULES_DEF";
            case OD_PARSED_RULE_MB:
                return "RAW_26_PARSED_RULE";
            case BENEFICIARY_MAINTENANCE:
                return "RAW_27_BENEFICIARY";
            case CIM_SUBPROD_ATTR_MAP_MB:
                return "RAW_29_CIM_SUBPROD";
        }
        return null;
    }

}
