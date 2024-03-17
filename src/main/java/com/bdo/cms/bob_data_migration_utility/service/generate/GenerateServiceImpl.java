package com.bdo.cms.bob_data_migration_utility.service.generate;

import static com.bdo.cms.bob_data_migration_utility.constant.Constants.BENEFICIARY_MAINTENANCE;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_BUSINESS_PARAMS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_CUST_ACCT_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_CUST_BILLING_DETAILS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_CUST_CONTACT_INFO_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_CUST_DEFN_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_DOMAIN_DEFN;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_PAYMENT_BKFT_DT_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_PAYMENT_CUST_PREF_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_PAYMENT_PARAMS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_SUBPROD_ATTR_MAP_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_CORPORATE_FUNCTION_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_CORPORATE_LIMITS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_PARSED_RULE_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_ROLES_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_RULES_ACC_MAP_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_RULES_DEF_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_RULES_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERGP_FUNCTION_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERGP_LIMITS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERGROUP_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USER_FUNCTION_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USER_LIMITS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USER_ROLES_MAP_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.ORBIIBS_NICKNAME;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.RECORDS_PER_BATCH;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.ROLES;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.RULES;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.FieldsAndTablesConfig;
import com.bdo.cms.bob_data_migration_utility.config.FileConfig;
import com.bdo.cms.bob_data_migration_utility.domain.BobUser;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.domain.UserLimits;
import com.bdo.cms.bob_data_migration_utility.domain.UserRoleMap;
import com.bdo.cms.bob_data_migration_utility.exception.CustomException;
import com.bdo.cms.bob_data_migration_utility.exception.FileServiceException;
import com.bdo.cms.bob_data_migration_utility.exception.GenerateException;
import com.bdo.cms.bob_data_migration_utility.exception.QueriesServiceException;
import com.bdo.cms.bob_data_migration_utility.exception.ResultSetParserException;
import com.bdo.cms.bob_data_migration_utility.service.DmService;
import com.bdo.cms.bob_data_migration_utility.service.custom.CustomService;
import com.bdo.cms.bob_data_migration_utility.service.custom.CustomServiceImpl;
import com.bdo.cms.bob_data_migration_utility.service.file.FileService;
import com.bdo.cms.bob_data_migration_utility.service.file.FileServiceImpl;
import com.bdo.cms.bob_data_migration_utility.service.parser.ResultSetParserService;
import com.bdo.cms.bob_data_migration_utility.service.queries.QueriesService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GenerateServiceImpl implements GenerateService {

    @Autowired
    BobDatabaseConfig bobCfg;

    @Autowired
    CustomService customService;

    @Autowired
    ResultSetParserService resultSetParserService;

    @Autowired
    FileService fileService;

    @Autowired
    QueriesService queriesService;
    
    @Autowired
    DmService dmService;
    
    @Autowired
    FieldsAndTablesConfig ftcfg;
    
    @Autowired
    FileConfig cfg;    

    @Override
    public Map<String, Integer> generatePerTable(String tableName, List<Input> inputs, Connection cib_connection, Connection cibut_connection) throws GenerateException {
        log.info("[{}] - GENERATE process started", tableName);

        List<String> records = new ArrayList<>();

        // for rules
        List<String> rules = new ArrayList<>();
        List<String> rulesDef = new ArrayList<>();
        List<String> parsedRule = new ArrayList<>();
        List<String> rulesAccMap = new ArrayList<>();

        // for roles
        List<String> roles = new ArrayList<>();
        List<String> userRoles = new ArrayList<>();

        try {
            
            int counter = 1;
            
            for (Input input : inputs) {

                log.info("[{}][{}/{}]|Processing input {} of {}",tableName, input.getCif(), input.getCorpCd(), counter, inputs.size());
                
//                boolean with_duplicate = queriesService.checkDuplicate(tableName, input);
//                if(!with_duplicate)
                switch (tableName) {
                    case CIM_CUST_BILLING_DETAILS_MB: 
                    case CIM_PAYMENT_CUST_PREF_MB:    
                        boolean with_duplicate = queriesService.checkDuplicate(tableName, input, cibut_connection);
                        if(with_duplicate)
                            log.info("[{}][{}/{}]|CIF already exists.", tableName, input.getCif(), input.getCorpCd());
                        else
                            records.addAll(insertDefaultValues(tableName, input, cibut_connection, cib_connection));
                        break;
                    case CIM_CUST_CONTACT_INFO_MB:
                        with_duplicate = queriesService.checkDuplicate(tableName, input, cibut_connection);
                        if(with_duplicate)
                            log.info("[{}][{}/{}]|CIF already exists.", tableName, input.getCif(), input.getCorpCd());
                        else
                            records.addAll(selectThenInsert(input, tableName, cib_connection, cibut_connection));
                        break;
                    default:
                        records.addAll(selectThenInsert(input, tableName, cib_connection, cibut_connection));
                }                    
                
                log.info("[{}][{}/{}]|Done Processing input {} of {}",tableName, input.getCif(), input.getCorpCd(), counter, inputs.size());
                counter++;
            }

            if (tableName.equals(RULES)) {
                for (String line : records) {
                    if (line.endsWith("rule")) {
                        rules.add(line.replace("rule", ""));
                    }
                    if (line.endsWith("rulesDef")) {
                        rulesDef.add(line.replace("rulesDef", ""));
                    }
                    if (line.endsWith("parsedRule")) {
                        parsedRule.add(line.replace("parsedRule", ""));
                    }
                    if (line.endsWith("rulesAcc")) {
                        rulesAccMap.add(line.replace("rulesAcc", ""));
                    }
                }
                fileService.write(rules, OD_RULES_MB);
                fileService.write(rulesDef, OD_RULES_DEF_MB);
                fileService.write(parsedRule, OD_PARSED_RULE_MB);
                fileService.write(rulesAccMap, OD_RULES_ACC_MAP_MB);

            } else if (tableName.equals(ROLES)) {
                for (String line : records) {
                    if (line.endsWith("role")) {
                        roles.add(line.replace("role", ""));
                    }
                    if (line.endsWith("userRole")) {
                        userRoles.add(line.replace("userRole", ""));
                    }
                }
                fileService.write(roles, OD_ROLES_MB);
                fileService.write(userRoles, OD_USER_ROLES_MAP_MB);

            } else {
                // write to output path
                fileService.write(records, tableName);
            }
            
            cib_connection.close();
            cibut_connection.close();

        }catch (CustomException | FileServiceException | SQLException | QueriesServiceException e
        ) {
            log.error(e.getMessage(), e);
            throw new GenerateException(e.getMessage(), e);
        }

        Map<String, Integer> recordsPerTable = new HashMap<>();

        if (tableName.equals(RULES)) {
            recordsPerTable.put(OD_RULES_MB, rules.size()-1);
            recordsPerTable.put(OD_RULES_DEF_MB, rulesDef.size()-1);
            recordsPerTable.put(OD_PARSED_RULE_MB, parsedRule.size()-1);
            recordsPerTable.put(OD_RULES_ACC_MAP_MB, rulesAccMap.size()-1);
        } else if (tableName.equals(ROLES)) {
            recordsPerTable.put(OD_ROLES_MB, roles.size()-1);
            recordsPerTable.put(OD_USER_ROLES_MAP_MB, userRoles.size()-1);

        } else {
            recordsPerTable.put(tableName, records.size()-1);
        }

        log.info("[{}] - GENERATE process ended", tableName);        

        return recordsPerTable;
    }


    
    private static Map<String, String[]> org_id_map;
    public static Map<String, String[]> get_org_id_mapping(String inputPath) throws GenerateException{
                
        if(org_id_map == null) {
            org_id_map = new HashMap<>();
            File file = new File(inputPath + "org_id_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String corp_org;
                while ((corp_org = br.readLine()) != null) {
                    
                    String[] details = corp_org.split("\\|");
                    if (details.length > 1) {
                        if(details.length >= 3) {
                            if(org_id_map.get(details[0].toUpperCase()) == null)
                                org_id_map.put(details[0].toUpperCase(), new String[]{details[1], details[2]});
                        } else {
                            log.error("Incomplete org details, please review the org mapping file.");
                        }
                    }
                    
                }
                
            } catch (IOException e) {
                log.info("Org Mapping file not found!");
                throw new GenerateException(e.getMessage(), e);
            }
            
            
        }
        return org_id_map;
    }    
    
    public List<String> insert(List<Map<String, String>> rs, String tableName, Input input, Connection cibut_connection) throws GenerateException {
        List<String> records = new ArrayList<>();
        try {
            if (!tableName.equals(RULES) && !tableName.equals(ROLES)) {
                switch (tableName) {
                    case CIM_PAYMENT_BKFT_DT_MB:
                    case CIM_PAYMENT_PARAMS_MB:
                    case CIM_BUSINESS_PARAMS_MB:
                    case OD_CORPORATE_FUNCTION_MB:
                    case OD_USER_FUNCTION_MB:
                    case OD_USERGP_FUNCTION_MB:
                    case OD_USERGROUP_MB:
                    case OD_USERGP_LIMITS_MB:
                    case BENEFICIARY_MAINTENANCE:  
                    case CIM_SUBPROD_ATTR_MAP_MB:
                        records = customService.customInsertToDb(rs, tableName, input, cibut_connection);
                        break;
                    default:
                        records = resultSetParserService.rsParserInsertToDb(rs, tableName, input, cibut_connection);
                }

            }
                        
            return records;

        } catch (ResultSetParserException | CustomException e) {
            log.error(e.getMessage(), e);
            throw new GenerateException(e.getMessage(), e);
        }
    }

    List<String> extracted_corps = new ArrayList<>();
        
    public List<String> selectThenInsert(Input input, String tableName, Connection cib_connection, Connection cibut_connection) throws GenerateException {
        List<String> records;
        String selectQuery = queriesService.getGnrtSelectQuery(tableName);
        try (                
                PreparedStatement preparedStatement = cib_connection.prepareStatement(selectQuery,
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_UPDATABLE)                
        ) {
            switch (tableName) {
                case CIM_CUST_DEFN_MB:
                    String value = get_org_id_mapping(cfg.inputPath).getOrDefault(input.getCorpCd(), new String[]{"", input.getCorpCd()})[1];
                    preparedStatement.setString(1, value);
                    preparedStatement.setString(2, input.getCorpCd());
                    preparedStatement.setString(3, input.getCif());
                    preparedStatement.setString(4, input.getCorpCd());
                    preparedStatement.setString(5, input.getCif());
                    break;
                case CIM_CUST_ACCT_MB:
                    preparedStatement.setString(1, input.getCif());
                    preparedStatement.setString(2, input.getCif());
                    break;
                case OD_USERGROUP_MB:
                case OD_CORPORATE_FUNCTION_MB:
                case OD_USER_LIMITS_MB:
                case OD_USERGP_LIMITS_MB:
                case OD_USERS_MB:
                case ORBIIBS_NICKNAME:                                
                    preparedStatement.setString(1, input.getCorpCd());
                    break;
                case OD_USER_FUNCTION_MB:                    
                    preparedStatement.setString(1, input.getCorpCd());
                    preparedStatement.setString(2, input.getCorpCd());
                    preparedStatement.setString(3, input.getCif());
                    preparedStatement.setString(4, extracted_corps.contains(input.getCorpCd()) ? "" : input.getCorpCd());                    
                    if(!extracted_corps.contains(input.getCorpCd()))
                        extracted_corps.add(input.getCorpCd());
                    break;
                case ROLES:
                    String regex = "(\\D[1-9]\\d{0,4})";                    
                    preparedStatement.setString(1, input.getCorpCd());
                    preparedStatement.setString(2, input.getCorpCd());
                    preparedStatement.setString(3, regex);
                    preparedStatement.setString(4, regex);
                    preparedStatement.setString(5, input.getCorpCd());
                    preparedStatement.setString(6, regex);
                    break;
                case OD_USERGP_FUNCTION_MB:
                    preparedStatement.setString(1, input.getCorpCd());
                    preparedStatement.setString(2, input.getCif());
                    preparedStatement.setString(3, input.getCorpCd());
                    break;
                case RULES:
                    preparedStatement.setString(1, input.getCorpCd());
                    break;                
                case CIM_PAYMENT_BKFT_DT_MB:
                case CIM_PAYMENT_PARAMS_MB:
                case CIM_BUSINESS_PARAMS_MB:
                case CIM_DOMAIN_DEFN:
                case CIM_SUBPROD_ATTR_MAP_MB:
                    preparedStatement.setString(1, input.getCorpCd());
                    preparedStatement.setString(2, input.getCif());                    
                    break;
                case CIM_CUST_CONTACT_INFO_MB:
                    preparedStatement.setString(1, input.getCorpCd());
                    preparedStatement.setString(2, input.getCif());
                    preparedStatement.setString(3, input.getCorpCd());
                    preparedStatement.setString(4, input.getCif());
                    preparedStatement.setString(5, input.getCorpCd());
                    preparedStatement.setString(6, input.getCif());
                    preparedStatement.setString(7, input.getCorpCd());
                    preparedStatement.setString(8, input.getCif());
                    preparedStatement.setString(9, input.getCorpCd());
                    preparedStatement.setString(10, input.getCif());
                    preparedStatement.setString(11, input.getCorpCd());
                    preparedStatement.setString(12, input.getCif());
                    preparedStatement.setString(13, input.getCorpCd());
                    preparedStatement.setString(14, input.getCif());
                    preparedStatement.setString(15, input.getCorpCd());
                    preparedStatement.setString(16, input.getCif());
                    break;
                case OD_CORPORATE_LIMITS_MB:
                    preparedStatement.setInt(1, FileServiceImpl.corpcifscount.get(input.getCorpCd()) * 100);
                    preparedStatement.setInt(2, FileServiceImpl.corpcifscount.get(input.getCorpCd()) * 30);
                    preparedStatement.setString(3, input.getCorpCd());
                    break;
                case BENEFICIARY_MAINTENANCE:
                    Thread.sleep(1L);                             
                    preparedStatement.setString(1, new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date()));
                    preparedStatement.setString(2, input.getCif());
                    preparedStatement.setString(3, input.getCorpCd());
                    preparedStatement.setString(4, input.getCif());
                    preparedStatement.setString(5, input.getCorpCd());
                    break;
                default:
                    preparedStatement.setString(1, input.getCorpCd());
                    preparedStatement.setString(2, input.getCif());
                    preparedStatement.setString(3, input.getCorpCd());
                    preparedStatement.setString(4, input.getCif());
            }

            ResultSet rs = preparedStatement.executeQuery();            
            
            List<Map<String, String>> rsmaplist = new ArrayList<>();
            int column_count = rs.getMetaData().getColumnCount();             
            
            while(rs.next()){                                
                
                Map<String, String> rsmap = new HashMap<>();
                for(int x = 1; x <= column_count; x++)
                {                                 
                    String column_name = rs.getMetaData().getColumnName(x);
                    String value = rs.getString(column_name);
                    
                    if("CIM_CUST_DEFN_MB".equalsIgnoreCase(tableName) && ("CUST_NAME".equalsIgnoreCase(column_name) 
                    												   || "ORG_NAME".equalsIgnoreCase(column_name)
                    												   || "CUST_NAME".equalsIgnoreCase(column_name)
                    												   || "CUST_NAME_LOC_LANG".equalsIgnoreCase(column_name)))
                    	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), false);
                    
                    if("CIM_DOMAIN_DEFN".equalsIgnoreCase(tableName) && ("DOMAIN_ID".equalsIgnoreCase(column_name)
                    												  || "DOMAIN_NAME".equalsIgnoreCase(column_name)))
						value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), false);
                    
                    if("CIM_CUST_CONTACT_INFO_MB".equalsIgnoreCase(tableName) && ("DOMAIN_ID".equalsIgnoreCase(column_name)
																			   || "NAME".equalsIgnoreCase(column_name)
																			   || "ADDRESS_LINE1".equalsIgnoreCase(column_name)
																			   || "ADDRESS_LINE2".equalsIgnoreCase(column_name)
																			   || "ADDRESS_LINE3".equalsIgnoreCase(column_name)))
                    	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), false);
                    
                    
                    if("CIM_CUST_ACCT_MB".equalsIgnoreCase(tableName) && ("ACCT_NAME".equalsIgnoreCase(column_name)
																	   || "ACCT_NAME_LOC_LANG".equalsIgnoreCase(column_name)
																	   || "ALIAS_NAME".equalsIgnoreCase(column_name)))
                    	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), false);
                    
                    if("OD_USER_FUNCTION_MB".equalsIgnoreCase(tableName) && ("OD_USER_NO".equalsIgnoreCase(column_name)))
                    	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), false);
                    
                    if(value != null)                       
                        value = value.replaceAll("\\|", "");
                                                                                                  
                    if(tableName.equals(CIM_CUST_CONTACT_INFO_MB) && column_name.startsWith("ADDRESS_LINE"))
                    {
                        if(column_name.equalsIgnoreCase("ADDRESS_LINE1") && value != null && !value.equalsIgnoreCase("N/A"))
                        {
                            String[] addr_arr = value.split(" ");
                            String addr_line = "";
                            int addr_arr_ctr = 1;
                            for(int y = 0; y < addr_arr.length; y++)
                            {
                                addr_line += addr_arr[y] + " ";
                                if(addr_line.length() + (y+1 == addr_arr.length ? 0 : addr_arr[y+1].length()) > 35 || y + 1 == addr_arr.length)
                                {
                                    String addr = addr_line.trim().replaceAll(",$", "");
                                    if(addr.length() > 35)
                                        addr = addr.substring(0, 35);

                                    rsmap.put("ADDRESS_LINE" + addr_arr_ctr, addr);
                                    addr_line = "";
                                    addr_arr_ctr++;
                                    if(addr_arr_ctr > 4)
                                        break;
                                }
                            }
                        } else if(column_name.equalsIgnoreCase("ADDRESS_LINE1") && value != null && value.equalsIgnoreCase("N/A")) rsmap.put(column_name, value);
                        
                    } else {
                        rsmap.put(column_name, value); 
                    }                                   
                    
                }
                rsmaplist.add(rsmap);                
            }
            
            rs.close();
            preparedStatement.close();            
            
            // insert result to temp tables in CIBUT
            records = insert(rsmaplist, tableName, input, cibut_connection);
            
            switch (tableName) {
                case RULES:
                    records = customService.rulesParsing(rsmaplist, tableName, input, cib_connection, cibut_connection);
                    break;
                case ROLES:
                    records = customService.rolesParsing(rsmaplist, tableName, input, cib_connection, cibut_connection);
                    break;
            }        
            
            

        } catch (SQLException | CustomException | InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new GenerateException(e.getMessage(), e);
        }

        return records;
    }           
    
    public List<String> insertDefaultValues(String tableName, Input input, Connection cibut_connection, Connection cib_connection) throws CustomException, SQLException{
        
        String defaultValues = queriesService.getGnrtSelectQuery(tableName);
        String[] fieldnValues = defaultValues.split("\\|");
        String fields = "";
        String values = "";
        Map<String, String> field_values = new HashMap<>();
        String insertQuery = "insert into " + tableName + " (fields) values(field_values)";                
        
        for(String fieldnValue : fieldnValues){
            
            String[] fieldnValueArray = fieldnValue.split(":");
            String field = fieldnValueArray[0];
            String value = fieldnValueArray.length > 1 ? fieldnValueArray[1] : null;
            
            fields += field + ", ";
            values += "?, ";
            
            field_values.put(field, value);

        }
        
        insertQuery = insertQuery.replace("fields", fields.substring(0, fields.lastIndexOf(", "))).replace("field_values", values.substring(0, values.lastIndexOf(", ")));
                
        List<String> records = customService.insertDefaultValuesToDb(insertQuery, tableName, field_values, input, cibut_connection, cib_connection);               
        
        return records;
    }

    public static Map<String, BobUser> bob_users = new HashMap<>();
    private static Map<String, String> phone_codes;
    
    @Override
    public Map<String, Integer> generateUserTables(List<Input> unique_corp_inputs, List<Input> inputs, Connection cib, Connection cibut) throws GenerateException {
        
        try {
            
//            Map<String, BobUser> bob_users = new HashMap<>();
            Map<String, BobUser> new_bob_users = new HashMap<>();
            
            String getBobUsersQuery = "select CONCAT(CONCAT(LOGIN_ID, '-'), ORG_ID) KEY, DOMAIN_ID VALUE from BOB_USERS";
            PreparedStatement ps = cibut.prepareStatement(getBobUsersQuery);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                bob_users.put(rs.getString("KEY"), BobUser.builder().domain_id(rs.getString("VALUE")).build());
            }
            rs.close();  
            ps.close();
            
            List<String> user_records = new ArrayList<>();
            List<String> orbiibs_records = new ArrayList<>();
                        
            List<BobUser> user_limits_records = new ArrayList<>();
            List<String> user_limits_records_str = new ArrayList<>();
            
            List<BobUser> user_functions_records = new ArrayList<>();
            List<String> user_functions_records_str = new ArrayList<>();
            
            List<String> roles_records = new ArrayList<>();
            
            for(Input input : unique_corp_inputs) {
                
                //users
                
                if(phone_codes == null) {
                    phone_codes = new HashMap<>();
                    File file = new File("config/phone_codes.csv");
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        br.readLine();
                        String phone_code_country;
                        while ((phone_code_country = br.readLine()) != null) {

                            String[] details = phone_code_country.replace("\"", "").split(",");                                        
                            phone_codes.put(details[0], details[1]);

                        }            
                    } catch (IOException e) {
                        log.info("Mapping file not found!");
                        throw new CustomException(e.getMessage(), e);
                    }
                }
                
                String selectQuery = queriesService.getGnrtSelectQuery(OD_USERS_MB);        
                ps = cib.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setString(1, input.getCorpCd());
                rs = ps.executeQuery();
                
                List<Map<String, String>> rsmaplist = new ArrayList<>();
                int column_count = rs.getMetaData().getColumnCount();
                
                while(rs.next()) {
                    String login_id = rs.getString("OD_LOGIN_ID");
                    
                    login_id = login_id.replaceAll("\\s",  ""); //remove whitespace characters
                    login_id = resultSetParserService.replaceSpecialCharacters(login_id, dmService.getSpecialCharacters(), true);
                    login_id = login_id.substring(0, Math.min(login_id.length(), 20)); //limit length to 20
                    if(login_id.length() < 8)
                    	login_id = resultSetParserService.padLoginId(login_id);
                    
                    
                    String org_id = get_org_id_mapping(cfg.inputPath).getOrDefault(input.getCorpCd(), new String[]{"", input.getCorpCd()})[0];
                    if(org_id == null || org_id.isEmpty())
                        org_id = input.getCorpCd();
                    String domain_id = rs.getString("OD_GCIF");
                    String key = login_id + "-" + org_id;                    
                    
                    BobUser bob_user = bob_users.get(key);
                    String default_domain = null;
                    
                    if(bob_user != null)
                        default_domain= bob_user.getDomain_id();
                    
                    if(default_domain != null)
                    {
                        domain_id = default_domain;
                    } else {
                        bob_users.put(key, BobUser.builder().domain_id(domain_id).login_id(login_id).org_id(org_id).build());
                        new_bob_users.put(key, BobUser.builder().domain_id(domain_id).login_id(login_id).org_id(org_id).build());
                    }
                    
                    Map<String, String> rsmap = new HashMap<>();
                    for(int x = 1; x <= column_count; x++) {                                 
                        String column_name = rs.getMetaData().getColumnName(x);
                        String value = rs.getString(column_name);
                        
                        if("FIRST_NAME".equalsIgnoreCase(column_name)
						|| "MIDDLE_NAME".equalsIgnoreCase(column_name)
						|| "LAST_NAME".equalsIgnoreCase(column_name)
						|| "ADDRESS_LINE1".equalsIgnoreCase(column_name)
						|| "ADDRESS_LINE2".equalsIgnoreCase(column_name)
						|| "RES_AREA".equalsIgnoreCase(column_name))
                        	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), false);
                        
                        if("OD_LOGIN_ID".equalsIgnoreCase(column_name)) {
                        	value = value.replaceAll("\\s",  ""); //remove whitespace characters
                        	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), true);
                        	value = value.substring(0, Math.min(value.length(), 20)); //limit length to 20
                        	
                        	if(value.length() < 8)
                        		value = resultSetParserService.padLoginId(value);
                        }

                        if(value != null)                       
                            value = value.replaceAll("\\|", "");

                        if(column_name.equalsIgnoreCase("OD_GCIF"))
                        {
                            value = domain_id;

                        }
                        
                        if(column_name.equalsIgnoreCase("COUNTRY"))
                        {
                            value = phone_codes.get(value);
                        }                                                 
                        
                        rsmap.put(column_name, value);  
                        
                        if(column_name.equalsIgnoreCase("ADDRESS_LINE1"))
                        {                                                    
                            if(value.length() <= 35) {

                                rsmap.put(column_name, value);

                            } else {

                                String[] addr_arr = value.split(" ");
                                String addr_line = "";
                                int addr_arr_ctr = 1;
                                for(int y = 0; y < addr_arr.length; y++)
                                {
                                    addr_line += addr_arr[y] + " ";
                                    if(addr_line.length() + (y+1 == addr_arr.length ? 0 : addr_arr[y+1].length()) > 35 || y + 1 == addr_arr.length)
                                    {
                                        String addr = addr_line.trim().replaceAll(",$", "");
                                        if(addr.length() > 35)
                                            addr = addr.substring(0, 35);

                                        rsmap.put("ADDRESS_LINE" + addr_arr_ctr, addr);                                        
                                        addr_line = "";
                                        addr_arr_ctr++;
                                        if(addr_arr_ctr > 2)
                                            break;
                                    }
                                }

                            }
                        }


                    }
//                    rsmap.put("OD_UDF9", input.getCif());
                    rsmaplist.add(rsmap);    
                    
                }
                
                rs.close();
                ps.close();
                user_records.addAll(insert(rsmaplist, OD_USERS_MB, input, cibut));
                
                //user_limits
                selectQuery = queriesService.getGnrtSelectQuery(OD_USER_LIMITS_MB);
                ps = cib.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setString(1, input.getCorpCd());
                rs = ps.executeQuery();                
                
                while(rs.next()) {
                    String login_id = rs.getString("OD_USER_NO");
                    login_id = login_id.replaceAll("\\s",  ""); //remove whitespace characters
                    login_id = resultSetParserService.replaceSpecialCharacters(login_id, dmService.getSpecialCharacters(), true);
                    login_id = login_id.substring(0, Math.min(login_id.length(), 20)); //limit string length to 20
                    if(login_id.length() < 8)
                    	login_id = resultSetParserService.padLoginId(login_id);
                    String org_id = get_org_id_mapping(cfg.inputPath).getOrDefault(input.getCorpCd(), new String[]{input.getCorpCd(), input.getCorpCd()})[0];
                    String domain_id = rs.getString("OD_GCIF");
                    String unit_id = rs.getString("UNIT_ID");
                    String unit_ccy = rs.getString("UNIT_CCY");
                    
                    BigDecimal od_daily_max_amt = rs.getBigDecimal("OD_DAILY_MAX_AMT");
                    int od_daily_max_no_trans = rs.getInt("OD_DAILY_MAX_NO_TRANS");
                    BigDecimal od_daily_max_amt_upl = rs.getBigDecimal("OD_DAILY_MAX_AMT");
                    int od_daily_max_no_trans_upl = rs.getInt("OD_DAILY_MAX_NO_TRANS");
                    BigDecimal od_daily_max_auth_amt = rs.getBigDecimal("OD_DAILY_MAX_AUTH_AMT");
                    BigDecimal od_self_auth_amt = rs.getBigDecimal("OD_SELF_AUTH_AMT");
                    String od_self_flag = rs.getString("OD_SELF_FLAG");
                    String od_approval_flag = rs.getString("OD_APPROVAL_FLAG");
                    String od_amt_masking_flag = rs.getString("OD_AMT_MASKING_FLAG");
                    int day_consolidated_txn_no = rs.getInt("DAY_CONSOLIDATED_TXN_NO");
                    BigDecimal day_consolidated_txn_amt = rs.getBigDecimal("DAY_CONSOLIDATED_TXN_AMT");
                    BigDecimal day_consol_max_approval_amt = rs.getBigDecimal("DAY_CONSOL_MAX_APPROVAL_AMT");
                    BigDecimal day_max_bulk_txn_approval_amt = rs.getBigDecimal("DAY_MAX_BULK_TXN_APPROVAL_AMT");
                    BigDecimal max_bulk_txn_approval_amt = rs.getBigDecimal("MAX_BULK_TXN_APPROVAL_AMT");
                    String txn_max_appr_limit_flag = rs.getString("TXN_MAX_APPR_LIMIT_FLAG");
                    BigDecimal txn_max_approval_amt = rs.getBigDecimal("TXN_MAX_APPROVAL_AMT");
                    String day_max_bulk_appr_limit_flag = rs.getString("DAY_MAX_BULK_APPR_LIMIT_FLAG");
                    String txn_max_bulk_appr_limit_flag = rs.getString("TXN_MAX_BULK_APPR_LIMIT_FLAG");
                    
                    String key = login_id + "-" + org_id;
                    BobUser bob_user = bob_users.get(key);                    
                    UserLimits bob_user_limits = null;
                    
                    if(bob_user != null) {
                        bob_user_limits = bob_user.getUser_limits();
                    
                        if(bob_user_limits != null)
                        {                            
                            if(bob_user_limits.getDay_consol_max_approval_amt().compareTo(day_consol_max_approval_amt) == -1) bob_user_limits.setDay_consol_max_approval_amt(day_consol_max_approval_amt);
                            if(bob_user_limits.getDay_consolidated_txn_amt().compareTo(day_consolidated_txn_amt) == -1) bob_user_limits.setDay_consolidated_txn_amt(day_consolidated_txn_amt);
                            if(bob_user_limits.getDay_consolidated_txn_no() > day_consolidated_txn_no) bob_user_limits.setDay_consolidated_txn_no(day_consolidated_txn_no);
                            if(bob_user_limits.getDay_max_bulk_appr_limit_flag().equalsIgnoreCase("Y") || day_max_bulk_appr_limit_flag.equalsIgnoreCase("Y")) bob_user_limits.setDay_max_bulk_appr_limit_flag("Y"); else bob_user_limits.setDay_max_bulk_appr_limit_flag("N");
                            if(bob_user_limits.getDay_max_bulk_txn_approval_amt().compareTo(day_max_bulk_txn_approval_amt) == -1) bob_user_limits.setDay_max_bulk_txn_approval_amt(day_max_bulk_txn_approval_amt);
                            if(bob_user_limits.getMax_bulk_txn_approval_amt().compareTo(max_bulk_txn_approval_amt) == -1) bob_user_limits.setMax_bulk_txn_approval_amt(max_bulk_txn_approval_amt);
                            if(bob_user_limits.getOd_amt_masking_flag().equalsIgnoreCase("Y") || od_amt_masking_flag.equalsIgnoreCase("Y")) bob_user_limits.setOd_amt_masking_flag("Y"); else bob_user_limits.setOd_amt_masking_flag("N");
                            if(bob_user_limits.getOd_approval_flag().equalsIgnoreCase("Y") || od_approval_flag.equalsIgnoreCase("Y")) bob_user_limits.setOd_approval_flag("Y"); else bob_user_limits.setOd_approval_flag("N");
                            if(bob_user_limits.getOd_daily_max_amt().compareTo(od_daily_max_amt) == -1) bob_user_limits.setOd_daily_max_amt(od_daily_max_amt);
                            if(bob_user_limits.getOd_daily_max_amt_upl().compareTo(od_daily_max_amt_upl) == -1) bob_user_limits.setOd_daily_max_amt_upl(od_daily_max_amt_upl);
                            if(bob_user_limits.getOd_daily_max_auth_amt().compareTo(od_daily_max_auth_amt) == -1) bob_user_limits.setOd_daily_max_auth_amt(od_daily_max_auth_amt);
                            if(bob_user_limits.getOd_daily_max_no_trans() < od_daily_max_no_trans) bob_user_limits.setOd_daily_max_no_trans(od_daily_max_no_trans);
                            if(bob_user_limits.getOd_daily_max_no_trans_upl() < od_daily_max_no_trans_upl) bob_user_limits.setOd_daily_max_no_trans_upl(od_daily_max_no_trans_upl);
                            if(bob_user_limits.getOd_self_auth_amt().compareTo(od_self_auth_amt) == -1) bob_user_limits.setOd_self_auth_amt(od_self_auth_amt);
                            if(bob_user_limits.getOd_self_flag().equalsIgnoreCase("Y") || od_self_flag.equalsIgnoreCase("Y")) bob_user_limits.setOd_self_flag("Y"); else bob_user_limits.setOd_self_flag("N");
                            if(bob_user_limits.getTxn_max_appr_limit_flag().equalsIgnoreCase("Y") || txn_max_appr_limit_flag.equalsIgnoreCase("Y")) bob_user_limits.setTxn_max_appr_limit_flag("Y"); else bob_user_limits.setTxn_max_appr_limit_flag("N");
                            if(bob_user_limits.getTxn_max_approval_amt().compareTo(txn_max_approval_amt) == -1) bob_user_limits.setTxn_max_approval_amt(txn_max_approval_amt);
                            if(bob_user_limits.getTxn_max_bulk_appr_limit_flag().equalsIgnoreCase("Y") || txn_max_bulk_appr_limit_flag.equalsIgnoreCase("Y")) bob_user_limits.setTxn_max_bulk_appr_limit_flag("Y"); else bob_user_limits.setTxn_max_bulk_appr_limit_flag("N");                                                    

                        } else {                            
                            bob_user_limits = UserLimits.builder()
                                    .input(input)
                                    .od_gcif(domain_id)
                                    .od_user_no(login_id)
                                    .unit_id(unit_id)
                                    .unit_ccy(unit_ccy)
                                    .day_consol_max_approval_amt(day_consol_max_approval_amt)
                                    .day_consolidated_txn_amt(day_consolidated_txn_amt)
                                    .day_consolidated_txn_no(day_consolidated_txn_no)
                                    .day_max_bulk_appr_limit_flag(day_max_bulk_appr_limit_flag)
                                    .day_max_bulk_txn_approval_amt(day_max_bulk_txn_approval_amt)
                                    .max_bulk_txn_approval_amt(max_bulk_txn_approval_amt)
                                    .od_amt_masking_flag(od_amt_masking_flag)
                                    .od_approval_flag(od_approval_flag)
                                    .od_daily_max_amt(od_daily_max_amt)
                                    .od_daily_max_amt_upl(od_daily_max_amt_upl)
                                    .od_daily_max_auth_amt(od_daily_max_auth_amt)
                                    .od_daily_max_no_trans_upl(od_daily_max_no_trans_upl)
                                    .od_daily_max_no_trans(od_daily_max_no_trans)
                                    .od_self_auth_amt(od_self_auth_amt)
                                    .od_self_flag(od_self_flag)
                                    .txn_max_appr_limit_flag(txn_max_appr_limit_flag)
                                    .txn_max_approval_amt(txn_max_approval_amt)
                                    .txn_max_bulk_appr_limit_flag(txn_max_bulk_appr_limit_flag)
                                    .build();                            
                        }
                        
                        bob_user.setUser_limits(bob_user_limits);                                                
                        
                        user_limits_records.add(bob_user);
                        
                    }
                    
                    
                    
                }
                
                rs.close();
                ps.close();      
                
                
                //orbiibs
                selectQuery = queriesService.getGnrtSelectQuery(ORBIIBS_NICKNAME);        
                ps = cib.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setString(1, input.getCorpCd());
                rs = ps.executeQuery();
                
                rsmaplist = new ArrayList<>();
                column_count = rs.getMetaData().getColumnCount();
                
                while(rs.next()) {
                    String login_id = rs.getString("LOGIN_ID");
                    login_id = login_id.replaceAll("\\s",  ""); //remove whitespace characters
                    login_id = resultSetParserService.replaceSpecialCharacters(login_id, dmService.getSpecialCharacters(), true);
                    login_id = login_id.substring(0, Math.min(login_id.length(), 20)); //limit string length to 20
                    if(login_id.length() < 8)
                    	login_id = resultSetParserService.padLoginId(login_id);
                    String org_id = get_org_id_mapping(cfg.inputPath).getOrDefault(input.getCorpCd(), new String[]{"", input.getCorpCd()})[0];
                    String domain_id = rs.getString("GCIF");
                    String key = login_id + "-" + org_id;
                    BobUser bob_user = bob_users.get(key);
                    String default_domain = null;
                    
                    if(bob_user != null)
                        default_domain = bob_user.getDomain_id();
                    
                    if(default_domain != null)
                    {
                        domain_id = default_domain;
                    }
                    
                    Map<String, String> rsmap = new HashMap<>();
                    for(int x = 1; x <= column_count; x++)
                    {                                 
                        String column_name = rs.getMetaData().getColumnName(x);
                        String value = rs.getString(column_name);
                        
                        if("LOGIN_ID".equalsIgnoreCase(column_name)) {
                        	value = value.replaceAll("\\s",  ""); //remove whitespace characters
                        	value = resultSetParserService.replaceSpecialCharacters(value, dmService.getSpecialCharacters(), true);
                        	value = value.substring(0, Math.min(value.length(), 20)); //limit length to 20
                        	if(value.length() < 8)
                        		value = resultSetParserService.padLoginId(value);
                        }

                        if(value != null)                       
                            value = value.replaceAll("\\|", "");

                        if(column_name.equalsIgnoreCase("GCIF"))
                        {
                            value = domain_id;

                        } 
                        
                        rsmap.put(column_name, value);                        


                    }
                    rsmaplist.add(rsmap);    
                    
                }
                
                rs.close();
                ps.close();
                orbiibs_records.addAll(insert(rsmaplist, ORBIIBS_NICKNAME, input, cibut));
                
                //roles
                selectQuery = queriesService.getGnrtSelectQuery(ROLES);
                ps = cib.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                String regex = "(\\D[1-9]\\d{0,4})";                    
                ps.setString(1, input.getCorpCd());
                ps.setString(2, input.getCorpCd());
                ps.setString(3, regex);
                ps.setString(4, regex);
                ps.setString(5, regex);
                ps.setString(6, regex);
                ps.setString(7, regex);
                ps.setString(8, regex);
                ps.setString(9, input.getCorpCd());
                ps.setString(10, regex);
                rs = ps.executeQuery();
                
                rsmaplist = new ArrayList<>();
                column_count = rs.getMetaData().getColumnCount();
                while(rs.next()){                                
                
                    Map<String, String> rsmap = new HashMap<>();
                    for(int x = 1; x <= column_count; x++)
                    {                                 
                        String column_name = rs.getMetaData().getColumnName(x);
                        String value = rs.getString(column_name);

                        if(value != null)                       
                            value = value.replaceAll("\\|", "");


                        rsmap.put(column_name, value);



                    }
                    rsmaplist.add(rsmap);                
                }
                
                rs.close();
                ps.close();
                
                //roles
                roles_records.addAll(customService.rolesParsing(rsmaplist, ROLES, input, cib, cibut));
                
            }            
            
            List<Map<String, String>> rsmaplist = new ArrayList<>();            
            for(BobUser bob_user : user_limits_records) {
                Map<String, String> rsmap = new HashMap<>();
                Input input = null;
                UserLimits user_limits = bob_user.getUser_limits();
                for(Field field : user_limits.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    String field_value = null;
                    Class field_type = field.getType();
                    if(field_type.getSimpleName().equalsIgnoreCase("BigDecimal"))
                        field_value = field.get(user_limits).toString();
                    else if(field_type.getSimpleName().equalsIgnoreCase("int"))
                        field_value = Integer.toString(field.getInt(user_limits));
                    else if(field_type.getSimpleName().equalsIgnoreCase("Input")) {
                        input = (Input) field.get(user_limits);
                        field_value = input.getCif() + "|" + input.getCorpCd();
                    }
                    else
                        field_value = (String) field.get(user_limits);
                    
                    rsmap.put(field.getName().toUpperCase(), field_value);
                }
                rsmaplist.add(rsmap);
                
                
            }
            
            user_limits_records_str.addAll(insert(rsmaplist, OD_USER_LIMITS_MB, null, cibut));                                 
            
            for(Input input : inputs) {
                user_functions_records_str.addAll(selectThenInsert(input, OD_USER_FUNCTION_MB, cib, cibut));
            }
                                    
            cibut.setAutoCommit(true);
            List<String> roles_map_records = new ArrayList<>();
            String query = "INSERT INTO OD_USER_ROLES_MAP_MB (OD_USER_NO, OD_GCIF, OD_ROLE_LEVEL, OD_USERGROUP_CODE, UNIT_ID) VALUES (?,?,?,?,?)";
            ps = cibut.prepareStatement(query);
            
            for(Map.Entry<String, UserRoleMap> entry : CustomServiceImpl.userRoleMap.entrySet()) {
                
                UserRoleMap urm = entry.getValue();
                ps.setString(1, urm.getOdUserNo());
                ps.setString(2, urm.getOdGcif());
                ps.setString(3, urm.getOdRoleLevel());
                ps.setString(4, urm.getOdUsergroupCode());
                ps.setString(5, urm.getUnitId());
                
                int result = ps.executeUpdate();
                
                if(result > 0) {
                    String line = urm.getOdUserNo() + "|" + urm.getOdGcif() + "|" + urm.getOdRoleLevel() + "|" + urm.getOdUsergroupCode()
                            + "|" + urm.getUnitId() + "||userRole";
                    roles_map_records.add(line);
                }
                
                
                
                
            }
            ps.close();
            
            roles_records.addAll(roles_map_records);
            
            
            
            fileService.write(user_records, OD_USERS_MB);
            fileService.write(orbiibs_records, ORBIIBS_NICKNAME);
            fileService.write(user_limits_records_str, OD_USER_LIMITS_MB);
            fileService.write(user_functions_records_str, OD_USER_FUNCTION_MB);
            
            // for roles
            List<String> roles = new ArrayList<>();
            List<String> userRoles = new ArrayList<>();
            for (String line : roles_records) {
                if (line.endsWith("role")) {
                    roles.add(line.replace("role", ""));
                }
                if (line.endsWith("userRole")) {
                    userRoles.add(line.replace("userRole", ""));
                }
            }
            fileService.write(roles, OD_ROLES_MB);
            fileService.write(userRoles, OD_USER_ROLES_MAP_MB);
            
            String insert_query = "insert into BOB_USERS(LOGIN_ID, ORG_ID, DOMAIN_ID) VALUES(?,?,?)";      
            ps = cibut.prepareStatement(insert_query);
            int record_ctr = 0;
            log.info("inserting {} new users...", new_bob_users.size());
            for (Map.Entry<String, BobUser> entry : new_bob_users.entrySet()) {
                
                BobUser value = entry.getValue();                
                ps.setString(1, value.getLogin_id());
                ps.setString(2, value.getOrg_id());
                ps.setString(3, value.getDomain_id());
                ps.addBatch();
                

                if ((record_ctr + 1) % RECORDS_PER_BATCH == 0 || record_ctr == new_bob_users.size()-1) {
                    log.info("executing batch...");
                    ps.executeBatch();
                }
                
                record_ctr++;
                
            }

            ps.close();
            
            
            
            Map<String, Integer> recordsPerTable = new HashMap<>();
            recordsPerTable.put(OD_USERS_MB, user_records.size()-1);   
            recordsPerTable.put(OD_USER_LIMITS_MB, user_limits_records_str.size()-1);
            recordsPerTable.put(ORBIIBS_NICKNAME, orbiibs_records.size()-1); 
            recordsPerTable.put(OD_USER_FUNCTION_MB, user_functions_records_str.size()-1);
            recordsPerTable.put(OD_ROLES_MB, roles.size()-1);
            recordsPerTable.put(OD_USER_ROLES_MAP_MB, userRoles.size()-1);

            return recordsPerTable;
            
            
        } catch (SQLException | FileServiceException | IllegalArgumentException | IllegalAccessException | CustomException e) {
            log.error(e.getMessage(), e);
            throw new GenerateException(e.getMessage(), e);
        } 
        
    }
}
