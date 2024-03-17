package com.bdo.cms.bob_data_migration_utility.service.parser;

import static com.bdo.cms.bob_data_migration_utility.constant.Constants.BENEFICIARY_MAINTENANCE;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_BUSINESS_PARAMS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_CUSTOMER_LIMIT_MB;
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
import static com.bdo.cms.bob_data_migration_utility.service.file.FileServiceImpl.getFields;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bdo.cms.bob_data_migration_utility.config.FieldsAndTablesConfig;
import com.bdo.cms.bob_data_migration_utility.config.FileConfig;
import com.bdo.cms.bob_data_migration_utility.config.QueriesConfig;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.ResultSetParserException;
import com.bdo.cms.bob_data_migration_utility.service.file.FileService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResultSetParserServiceImpl implements ResultSetParserService{
    @Autowired
    FileService fileService;

    @Autowired
    FileConfig fileConfig;

    @Autowired
    FieldsAndTablesConfig fieldsCfg;
    
    @Autowired
    QueriesConfig qcfg;

    @Override
    public List<String> parseToString(List<Map<String, String>> rs, String tableName) throws ResultSetParserException {
        Map<Integer, String> fieldMap = fileService.fieldMap(fetchFields(tableName));
        List<String> records = new ArrayList<>();
        try {
            for(Map<String, String> map : rs) {
                StringBuilder line = new StringBuilder();
                int size = fieldMap.size();
                for (int i = 1; i <= size; i++) {
                    String field = fieldMap.get(i);
                    String value = null;
                    value = map.get(field);
                    if (isDate(field) && value != null) {
                        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(value);
                        value = new SimpleDateFormat(fileConfig.dateFormat).format(date);
                    }
                    line.append(value != null ? value : "").append(i != size ? fileConfig.outputDelimiter : "");
                }
                records.add(line.toString());
            }
        } catch (ParseException e) {
            log.error(tableName + "|" + e.getMessage(), e);
            throw new ResultSetParserException(e.getMessage(), e);
        }

        return records;
    }

    @Override
    public List<String> rsParserInsertToDb(List<Map<String, String>> rs, String tableName, Input input, Connection connection) throws ResultSetParserException {                        
            
        List<String> fields = fetchFields(tableName);
        List<String> lines = new ArrayList<>();
        List<String> records = new ArrayList<>();
        List<Map<String, String>> rs_processed = new ArrayList<>();
        
        if(input == null) {
                    input = Input.builder()
                            .cif("*")
                            .corpCd("*")
                            .build();
                }
        
        try {
            for(Map<String, String> map : rs) {
            	
//              added by dave 12182023
            	String strbVal = "";
//            	added by dave 12182023
                
                StringBuilder f = new StringBuilder();
                StringBuilder v = new StringBuilder();
                for (int i = 0; i < fields.size(); i++) {
                    String value = null;
                    String field = fields.get(i).toUpperCase();
                    
                        value = map.get(field);
                    
                    if (isDate(field) && value != null) {
                        Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(value);
                        value = "TO_DATE('" + new SimpleDateFormat(fileConfig.dateFormat).format(date)
                                + "', '" + fileConfig.dateFormat + "')";
                    }
                    
//                    added by dave 12182023
                    if (field.equals("OD_LOGIN_ID")) {
                    	if (strbVal == null || strbVal.trim().length()<=0) {
                    		strbVal = value;
                    	}
                    	else {
                    		strbVal= strbVal + ", " + value;
                    	}
                    }
//                    added by dave 12182023
                    
                    f.append(field).append(i == fields.size()-1 ? "" : ",");
                    v.append((isDate(field) && value!=null) ? "" : "q'[").append(value == null ? "" : value)
                        .append((isDate(field) && value!=null) ? "" : "]'").append(i == fields.size()-1 ? "" : ",");
                                                                                

                }
                String line = "INSERT INTO " + tableName + " (" + f + ") VALUES (" + v + ")";                            
                
                PreparedStatement ps = null;
                try {
                    ps = connection.prepareStatement(line);
                    ps.executeQuery();
                    ps.close();
                    rs_processed.add(map);
                } catch (SQLIntegrityConstraintViolationException e) {
                    
                    if(ps != null)
                        ps.close();
                    log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]| Insert Failed : " + e.getMessage().trim(),tableName);
                    
//                    added by dave 12152023
                    if (tableName.equals(OD_USERS_MB)) {
                        fileService.createErrReport( input.getCif(),  input.getCorpCd(), tableName ,strbVal , "OD_LOGIN_ID", e.getMessage().trim(),  "GENERATE");
                    }
//                  added by dave 12152023
                     
                }
                
                
                lines.add(line);
            }
            
        } catch (ParseException | SQLException e) {
            log.error(e.getMessage(), e);
            throw new ResultSetParserException(e.getMessage(), e);
        }


        if(!rs_processed.isEmpty()) {
            records = parseToString(rs_processed, tableName);
            log.info("[{}][{}/{}]|Insert Successful : {} out of {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), rs_processed.size(), rs.size());
        }
        else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
        
//        if (!lines.isEmpty()) {
//            
//            try {
//                
//                StringBuilder insertQuery = new StringBuilder("INSERT ALL \n");
//                for (String l : lines) {                    
//                    insertQuery.append(l).append("\n");                    
//                }
//                insertQuery.append("SELECT * FROM DUAL");
//
//                PreparedStatement ps = connection.prepareStatement(insertQuery.toString());
//                ps.execute();                
//                records = parseToString(rs, tableName);
//                ps.close();
//                log.info("[{}][{}/{}]|Batch Insert Successful : {} records inserted.", tableName, input.getCif(), input.getCorpCd(), lines.size());
//
//            } catch(SQLIntegrityConstraintViolationException e) {
//                log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to " + e.getMessage().trim(),tableName, lines.size());                                                                        
//            } catch (SQLException e) {
//                log.error(e.getMessage(), e);
//                throw new ResultSetParserException(e.getMessage(), e);
//            }
//
//        } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());

        return records;

    }
    
    
    

    private boolean isDate(String field) {
        for (String s : fileConfig.dateKeywords) {
            if(field.equalsIgnoreCase("DEBIT_BASED_ON_DATE"))
                return false;
            if (field.endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private List<String> fetchFields(String tableName) {
        List<String> fields;
        switch (tableName) {
            case CIM_CUST_DEFN_MB:
                fields = fieldsCfg.cimCustDefnMbFields;                
                break;
            case CIM_CUST_ACCT_MB:
                fields = fieldsCfg.cimCustAcctMbFields;
                break;
            case CIM_CUST_CONTACT_INFO_MB:
                fields = fieldsCfg.cimCustContactInfoMbFields;
                break;
            case CIM_DOMAIN_DEFN:
                fields = fieldsCfg.cimDomainDefnFields;
                break;
            case CIM_CUSTOMER_LIMIT_MB:
                fields = fieldsCfg.cimCustomerLimitMbFields;
                break;
            case OD_CORPORATE_LIMITS_MB:                
                fields = fieldsCfg.odCorporateLimitsMbFields;
                break;
            case OD_USERGROUP_MB:
                fields = fieldsCfg.odUsergroupMbFields;
                break;
            case OD_CORPORATE_FUNCTION_MB:
                fields = fieldsCfg.odCorporateFunctionMbFields;
                break;
            case OD_USERS_MB:
                fields = fieldsCfg.odUsersMbFields;
                break;
            case OD_USER_LIMITS_MB:                
                fields = fieldsCfg.odUserLimitsMbFields;
                break;
            case ORBIIBS_NICKNAME:
                fields = fieldsCfg.orbiibsNicknameFields;
                break;
            case OD_USER_FUNCTION_MB:
                fields = fieldsCfg.odUserFunctionMbFields;
                break;
            case OD_USERGP_FUNCTION_MB:
                fields = fieldsCfg.odUsergpFunctionMbFields;
                break;
            case OD_USERGP_LIMITS_MB:
                fields = fieldsCfg.odUsergpLimitsMbFields;
                break;
            case OD_ROLES_MB:
                fields = fieldsCfg.odRolesMbFields;
                break;
            case OD_RULES_MB:
                fields = fieldsCfg.odRulesMbFields;
                break;
            case OD_RULES_DEF_MB:
                fields = fieldsCfg.odRulesDefMbFields;
                break;
            case OD_PARSED_RULE_MB:
                fields = fieldsCfg.odParsedRuleMbFields;
                break;
            case OD_RULES_ACC_MAP_MB:
                fields = fieldsCfg.odRulesAccMapMbFields;
                break;
            case OD_USER_ROLES_MAP_MB:
                fields = fieldsCfg.odUserRolesMapMbFields;
                break;
            case BENEFICIARY_MAINTENANCE:
                fields = fieldsCfg.beneficiaryMaintenanceFields;
                break;
            case CIM_CUST_BILLING_DETAILS_MB:
                String[] fieldsArray = qcfg.gnrtSelectCimCustBillingDetailsMb.split("\\|");
                fields = getFields(fieldsArray);
                break;
            case CIM_PAYMENT_BKFT_DT_MB:                
                fields = fieldsCfg.paymntProductsCimPaymentBkftDtMbFields;
                break;
            case CIM_PAYMENT_CUST_PREF_MB:
                fieldsArray = qcfg.gnrtSelectCimPaymentCustPrefMb.split("\\|");
                fields = getFields(fieldsArray);
                break;
            case CIM_PAYMENT_PARAMS_MB:                
                fields = fieldsCfg.paymntProductsCimPaymentParamsMbFields;
                break;
            case CIM_BUSINESS_PARAMS_MB:                
                fields = fieldsCfg.paymntProductsCimBusinessParamsMbFields;
                break;
            case CIM_SUBPROD_ATTR_MAP_MB:
                fields = fieldsCfg.cimSubProdAttrMapMbFields;
                break;
            default:
                fields = new ArrayList<>();
        }
        return fields;
    }

    public String replaceSpecialCharacters(String string, Map<String, String> specialCharacters, boolean isUsersTable) { //replace special characters  of LOGIN IDs in OD_USERS_MB, OD_USER_LIMITS_MB and ORBIIBS_NICKNAME with replacements map
    	if (string != null && specialCharacters != null && !specialCharacters.isEmpty()) { //else substitute replacement from special characters table
    		if (!string.isEmpty())
    			if(isUsersTable) {
    				Map<Character, String> replacements = new HashMap<>();
    		    	replacements.put('~',"T");
    		    	replacements.put('!',"1");
    		    	replacements.put('@',"2");
    		    	replacements.put('#',"3");
    		    	replacements.put('$',"4");
    		    	replacements.put('%',"5");
    		    	replacements.put('^',"6");
    		    	replacements.put('&',"7");
    		    	replacements.put('*',"8");
    		    	replacements.put('(',"9");
    		    	replacements.put(')',"9");
    		    	replacements.put('_',"0");
    		    	replacements.put('-',"H");
    		    	replacements.put('+',"P");
    		    	replacements.put('=',"E");
    		    	replacements.put('{',"OB");
    		    	replacements.put('}',"CC");
    		    	replacements.put('[',"OS");
    		    	replacements.put(']',"CS");
    		    	replacements.put('|',"V");
    		    	replacements.put('\\',"B");
    		    	replacements.put(':',"L");
    		    	replacements.put(';',"S");
    		    	replacements.put('"',"DQ");
    		    	replacements.put('\'',"SQ");
    		    	replacements.put('<',"LT");
    		    	replacements.put('>',"GT");
    		    	replacements.put(',',"C");
    		    	replacements.put('.',"P");
    		    	replacements.put('/',"F");
    		    	replacements.put('?',"Q");
    		    	
    		    	StringBuilder replaced = new StringBuilder();
    		    	for (char c : string.toCharArray()) {
    		    		if (replacements.containsKey(c)) {
    		    			replaced.append(replacements.get(c));
    		    		} else {
    		    			replaced.append(c);
    		    		}
    		    	}
    		    	string = replaced.toString();
    				
    			} else {
					for (Map.Entry<String, String> entry : specialCharacters.entrySet()) {
						String specialCharacter = entry.getKey();
						String replacement = entry.getValue();
						
						if (string.contains(specialCharacter))
							if (replacement == null || replacement.isEmpty()) {
								string = string.replace(specialCharacter, "");
							} else {
								string = string.replace(specialCharacter, replacement);
							}
					}
    			}
    	} else {
    		string = "";
    	}
    	return string;
    }
    
    public String padLoginId(String loginId) {
        if (loginId.length() < 8) { //pad login id with 'x' until it's 8 characters in length
        	int paddingLength = 8 - loginId.length();
        	StringBuilder padding = new StringBuilder();
        	for (int i = 0; i < paddingLength; i++) {
        		padding.append('X');
        	}
        	loginId = loginId + padding.toString();
        }
        return loginId;
    }
}
