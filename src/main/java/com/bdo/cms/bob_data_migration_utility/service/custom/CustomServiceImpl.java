package com.bdo.cms.bob_data_migration_utility.service.custom;

import static com.bdo.cms.bob_data_migration_utility.constant.Constants.BENEFICIARY_MAINTENANCE;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_BUSINESS_PARAMS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_CUST_BILLING_DETAILS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_PAYMENT_BKFT_DT_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_PAYMENT_PARAMS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CIM_SUBPROD_ATTR_MAP_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_CORPORATE_FUNCTION_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERGP_FUNCTION_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERGP_LIMITS_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USERGROUP_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.OD_USER_FUNCTION_MB;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.RECORDS_PER_BATCH;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.ROLES;
import static com.bdo.cms.bob_data_migration_utility.service.generate.GenerateServiceImpl.get_org_id_mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.paukov.combinatorics3.Generator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.FieldsAndTablesConfig;
import com.bdo.cms.bob_data_migration_utility.config.FileConfig;
import com.bdo.cms.bob_data_migration_utility.config.QueriesConfig;
import com.bdo.cms.bob_data_migration_utility.domain.BeneficiaryMaintenance;
import com.bdo.cms.bob_data_migration_utility.domain.BobUser;
import com.bdo.cms.bob_data_migration_utility.domain.CimBusinessParamsMb;
import com.bdo.cms.bob_data_migration_utility.domain.CimPaymentBkftDtMb;
import com.bdo.cms.bob_data_migration_utility.domain.CimPaymentParamsMb;
import com.bdo.cms.bob_data_migration_utility.domain.CimSubProdAttrMapMb;
import com.bdo.cms.bob_data_migration_utility.domain.CorporateFunction;
import com.bdo.cms.bob_data_migration_utility.domain.FunctionId;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.domain.ParsedRule;
import com.bdo.cms.bob_data_migration_utility.domain.Role;
import com.bdo.cms.bob_data_migration_utility.domain.Rule;
import com.bdo.cms.bob_data_migration_utility.domain.RuleAcctContainer;
import com.bdo.cms.bob_data_migration_utility.domain.RuleContainer;
import com.bdo.cms.bob_data_migration_utility.domain.Rules;
import com.bdo.cms.bob_data_migration_utility.domain.RulesAccMap;
import com.bdo.cms.bob_data_migration_utility.domain.RulesDef;
import com.bdo.cms.bob_data_migration_utility.domain.UserFunction;
import com.bdo.cms.bob_data_migration_utility.domain.UserRoleContainer;
import com.bdo.cms.bob_data_migration_utility.domain.UserRoleMap;
import com.bdo.cms.bob_data_migration_utility.domain.UsergpFunction;
import com.bdo.cms.bob_data_migration_utility.domain.UsergpLimit;
import com.bdo.cms.bob_data_migration_utility.domain.Usergroup;
import com.bdo.cms.bob_data_migration_utility.exception.CustomException;
import com.bdo.cms.bob_data_migration_utility.exception.GenerateException;
import com.bdo.cms.bob_data_migration_utility.service.DmService;
import com.bdo.cms.bob_data_migration_utility.service.DmServiceImpl;
import com.bdo.cms.bob_data_migration_utility.service.file.FileService;
import com.bdo.cms.bob_data_migration_utility.service.generate.GenerateServiceImpl;
import com.bdo.cms.bob_data_migration_utility.service.parser.ResultSetParserService;
import com.bdo.cms.bob_data_migration_utility.service.queries.QueriesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomServiceImpl implements CustomService {

    @Autowired
    BobDatabaseConfig bobcfg;

    @Autowired
    FileConfig fcfg;

    @Autowired
    QueriesConfig qcfg;

    @Autowired
    FieldsAndTablesConfig ftcfg;
    
    @Autowired
    FileService fileService;
    
    @Autowired
    ResultSetParserService resultSetParserService;
    
    @Autowired
    ResultSetParserService resultSetParser;
    
    @Autowired
    DmService dmService;
    
    @Autowired
    FileConfig cfg;
    
    @Autowired
    QueriesService queriesService;

    @Override
    public List<String> customInsertToDb(List<Map<String, String>> rs, String tableName, Input input, Connection cibut_connection) throws CustomException {

        boolean isEmpty;
        
        List<String> records = new ArrayList<>();

        switch (tableName) {
            case CIM_PAYMENT_BKFT_DT_MB:
                List<CimPaymentBkftDtMb> defaultCimPaymentBkftDtMbDefaultEntries = getCimPaymentBkftDtMbDefaultEntries(rs);
                if(!defaultCimPaymentBkftDtMbDefaultEntries.isEmpty()) {
                    
                    try {
                        
                        String query = "insert into CIM_PAYMENT_BKFT_DT_MB (CUST_ID, PAYMENT_PRODUCT, BACK_DT_TXNS_ALLOWED, BACK_DT_DAYS, FUTURE_DT_TXNS_ALLOWED, FUTURE_DT_DAYS) VALUES (?,?,?,?,?,?)";
                        PreparedStatement ps = cibut_connection.prepareStatement(query);

                        for(CimPaymentBkftDtMb defaultEntry : defaultCimPaymentBkftDtMbDefaultEntries) {
                            ps.setString(1, input.getCif());
                            ps.setString(2, defaultEntry.getPaymentProduct());
                            ps.setString(3, defaultEntry.getBackDtTxnsAllowed());
                            ps.setString(4, defaultEntry.getBackDtDays());
                            ps.setString(5, defaultEntry.getFutureDtTxnsAllowed());
                            ps.setString(6, defaultEntry.getFutureDtDays());
                            ps.addBatch();                            
                            
                        }

                        ps.executeBatch();
                        ps.close();
                        records = parseToString(rs, tableName, input, cibut_connection);
                        
                    } catch (SQLException ex) {
                        log.error(ex.getMessage(), ex);
                        throw new CustomException(ex.getMessage(), ex);
                    }
                    
                } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;

            case CIM_PAYMENT_PARAMS_MB:
                List<CimPaymentParamsMb> defaultCimPaymentParamsMbEntries = getCimPaymentParamsMbDefaultEntries(rs);
                if(!defaultCimPaymentParamsMbEntries.isEmpty()) {
                    
                    try {
                        
                        String query = "insert into CIM_PAYMENT_PARAMS_MB (CUST_ID, PAYMENT_PRODUCT, EXT_CUTOFF_TIME_APPLICABLE, EXT_CUTOFF_TIME, "
                                + "DEBIT_ARR_DAYS, DEBIT_BASED_ON_DATE, HOLD_DEBIT_DAYS, SPLITTING_ALLOWED, SPLITTING_THRESHOLD_AMT, "
                                + "SLA_REQ, SLA_MINUTES) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement ps = cibut_connection.prepareStatement(query);
                        
                        for(CimPaymentParamsMb defaultEntry : defaultCimPaymentParamsMbEntries) {
                            
                            ps.setString(1, input.getCif());
                            ps.setString(2, defaultEntry.getPaymentProduct());
                            ps.setString(3, defaultEntry.getExtCutoffTimeApplicable());
                            ps.setString(4, defaultEntry.getExtCutoffTime());
                            ps.setString(5, defaultEntry.getDebitArrDays());
                            ps.setString(6, defaultEntry.getDebitBasedOnDate());
                            ps.setString(7, defaultEntry.getHoldDebitDays());
                            ps.setString(8, defaultEntry.getSplittingAllowed());
                            ps.setString(9, defaultEntry.getSplittingThresholdAmt());
                            ps.setString(10, defaultEntry.getSlaReq());
                            ps.setString(11, defaultEntry.getSlaMinutes());
                            ps.addBatch();
                        }
                        
                        ps.executeBatch();
                        ps.close();
                        records = parseToString(rs, tableName, input, cibut_connection);
                        
                    } catch (SQLException ex) {
                        log.error(ex.getMessage(), ex);
                        throw new CustomException(ex.getMessage(), ex);
                    }
                    
                } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;
                
            case CIM_BUSINESS_PARAMS_MB:
                List<CimBusinessParamsMb> defaultCimBusinessParamsMbDefaultEntries = getCimBusinessParamsMbsDefaultEntries(rs);
                if(!defaultCimBusinessParamsMbDefaultEntries.isEmpty()) {
                    
                    try{
                        
                        String query = "insert into CIM_BUSINESS_PARAMS_MB (CUST_ID, BUSINESS_PRODUCT, ONLY_REG_BENE_FOR_TXN, DEBIT_LVL_ID, BATCH_RULE_ID, BATCH_RULE_NAME) VALUES (?,?,?,?,?,?)";
                        PreparedStatement ps = cibut_connection.prepareStatement(query);
                        
                        for(CimBusinessParamsMb defaultEntry : defaultCimBusinessParamsMbDefaultEntries) {
                            
                            ps.setString(1, input.getCif());
                            ps.setString(2, defaultEntry.getBusinessProduct());
                            ps.setString(3, defaultEntry.getOnlyRegBeneForTxn());
                            ps.setString(4, defaultEntry.getDebitLvlId());
                            ps.setString(5, defaultEntry.getBatchRuleId());
                            ps.setString(6, defaultEntry.getBatchRuleName());
                            ps.addBatch();
                            
                        }
                        
                        ps.executeBatch();
                        ps.close();
                        records = parseToString(rs, tableName, input, cibut_connection);
                        
                    } catch (SQLException ex) {
                        log.error(ex.getMessage(), ex);
                        throw new CustomException(ex.getMessage(), ex);
                    }
                    
                } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;
                
            case CIM_SUBPROD_ATTR_MAP_MB:
                List<CimSubProdAttrMapMb> defaultCimSubProdAttrMapMbEntries = getCimSubProdAttrMapMbDefaultEntries(rs);
                if(!defaultCimSubProdAttrMapMbEntries.isEmpty()) {
                
                    try{
                        
                        String query = "insert into CIM_SUBPROD_ATTR_MAP_MB (CUST_ID, SUBPRODUCT, PRODUCT, ATTR_LEVEL, TYPE, SUBPRODUCT_NAME) VALUES(?,?,?,?,?,?)";
                        PreparedStatement ps = cibut_connection.prepareStatement(query);
                        
                        for(CimSubProdAttrMapMb defaultEntry : defaultCimSubProdAttrMapMbEntries) {
                            
                            ps.setString(1, input.getCif());
                            ps.setString(2, defaultEntry.getSubProduct().split("-")[0]);
                            ps.setString(3, defaultEntry.getProduct());
                            ps.setString(4, defaultEntry.getAttrLevel());
                            ps.setString(5, defaultEntry.getType());
                            ps.setString(6, defaultEntry.getSubProductName());
                            ps.addBatch();
                            
                        }
                        
                        ps.executeBatch();
                        ps.close();
                        records = parseToString(rs, tableName, input, cibut_connection);
                        
                    } catch (SQLException ex) {
                        log.error(ex.getMessage(), ex);
                        throw new CustomException(ex.getMessage(), ex);
                    }
                    
                } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;
                
            case OD_CORPORATE_FUNCTION_MB:                                                
                List<CorporateFunction> corpFunctions = getCorporateFunctions(rs);
                isEmpty = corpFunctions.isEmpty();
                if (!isEmpty) {
                    try (
                            PreparedStatement ps = cibut_connection.prepareStatement(
                                    "INSERT INTO OD_CORPORATE_FUNCTION_MB (OD_GCIF, OD_FUNCTION_CODE, OD_PRODUCT_CODE, OD_SUBPROD_CODE, VERIFICATION_REQ, RELEASE_REQ) VALUES (?,?,?,?,?,?)"
                            )
                    ) {
                        for (int i = 0; i < corpFunctions.size(); i++) {
                            CorporateFunction cf = corpFunctions.get(i);
                            ps.setString(1, input.getCorpCd());
                            ps.setString(2, cf.getOdFunctionCode());
                            ps.setString(3, cf.getOdProductCode());
                            ps.setString(4, cf.getOdSubprodCode());
                            ps.setString(5, cf.getVerificationReq());
                            ps.setString(6, cf.getReleaseReq());
                            ps.addBatch();

                            if ((i + 1) % RECORDS_PER_BATCH == 0 || i == corpFunctions.size() - 1) {
                                ps.executeBatch();                                
                                records = parseToString(rs, tableName, input, cibut_connection);
                                log.info("[{}][{}/{}]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), corpFunctions.size());
                            }
                        }
                        
                        ps.close();

                    } catch(BatchUpdateException e) {  
                        
                        log.error("[" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to " + e.getMessage().trim(), corpFunctions.size());                            
                        
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                        throw new CustomException(e.getMessage(), e);
                    }
                } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;
            case OD_USER_FUNCTION_MB:
                                
                List<UserFunction> userFunctions = getUserFunctions(rs, input);
                isEmpty = userFunctions.isEmpty();
                PreparedStatement ps_user_function = null;
                if (!isEmpty) {
                    try {
                            cibut_connection.setAutoCommit(false);
                            String query = "INSERT INTO OD_USER_FUNCTION_MB (OD_GCIF, OD_USER_NO, OD_FUNCTION_CODE, OD_PRODUCT_CODE, OD_SUBPROD_CODE, OD_ACC_NO, CRITERIA_TYPE, UNIT_ID) VALUES (?,?,?,?,?,?,?,?)";
                            
                    
                        for (int i = 0; i < userFunctions.size(); i++) {
                            ps_user_function = cibut_connection.prepareStatement(query);
                            UserFunction uf = userFunctions.get(i);
                            ps_user_function.setString(1, uf.getOd_gcif());
                            ps_user_function.setString(2, uf.getOd_user_no());
                            ps_user_function.setString(3, uf.getOd_function_code());
                            ps_user_function.setString(4, uf.getOd_product_code());
                            ps_user_function.setString(5, uf.getOd_subprod_code());
                            ps_user_function.setString(6, uf.getOd_acc_no());
                            ps_user_function.setString(7, uf.getCriteria_type());
                            ps_user_function.setString(8, uf.getUnit_id());

                            try {
                                int result = ps_user_function.executeUpdate();
                                if(result > 0){
                                    Map<String, String> rsmap = uf.getRs();
                                    String line = input.getCorpCd() + "|" + rsmap.get("OD_USER_NO") + "|" + uf.getOd_function_code()
                                        + "|" + uf.getOd_product_code() + "|" + uf.getOd_subprod_code() + "|" + rsmap.get("OD_ACC_NO")
                                        + "|" + uf.getCriteria_type() + "|" + uf.getUnit_id();
                                    records.add(line);
                                }
                            } catch (SQLIntegrityConstraintViolationException ex) {
//                                log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]| Insert Failed : " + ex.getMessage().trim(),tableName);
                            } finally {
                                
                                ps_user_function.close();
                            }

                            if ((i + 1) % RECORDS_PER_BATCH == 0 || i == userFunctions.size() - 1) {                            
                                
                                cibut_connection.commit();
                                
                            }
                        }        
                                               
                        log.info("[{}][{}/{}]|Batch Insert Successful : {} record/s inserted.",tableName, input.getCif(), input.getCorpCd(), userFunctions.size());

                    } catch (SQLException e) {
                        try {
                            cibut_connection.rollback();
                        } catch (SQLException ex) {
                            throw new CustomException(e.getMessage(), e);
                        }
                        log.error(e.getMessage(), e);
                        throw new CustomException(e.getMessage(), e);
                    }
                    
                } else log.info("[{}][{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;

            case OD_USERGP_FUNCTION_MB:
                List<UsergpFunction> usergpFunctions = getUsergpFunctions(input, rs, cibut_connection);
                isEmpty = usergpFunctions.isEmpty();
                if (!isEmpty) {
                    try (
                            PreparedStatement ps = cibut_connection.prepareStatement(
                                    "INSERT INTO OD_USERGP_FUNCTION_MB (OD_USERGROUP_CODE, OD_FUNCTION_CODE, OD_PRODUCT_CODE, OD_SUBPROD_CODE, OD_ACC_NO, CRITERIA_TYPE, UNIT_ID)"
                                            + " SELECT ?, ?, ?, ?, ?, ?, ? FROM DUAL"
                                            + " WHERE NOT EXISTS(SELECT * FROM OD_USERGP_FUNCTION_MB WHERE OD_USERGROUP_CODE = ? AND OD_FUNCTION_CODE = ? AND OD_PRODUCT_CODE = ?"
                                            + " AND OD_SUBPROD_CODE = ? AND OD_ACC_NO = ? AND CRITERIA_TYPE = ?)"
                            )
                    ) {
                        for (int i = 0; i < usergpFunctions.size(); i++) {
                            UsergpFunction ugf = usergpFunctions.get(i);
                            ps.setString(1, ugf.getOdUsergroupCode());
                            ps.setString(2, ugf.getOdFunctionCode());
                            ps.setString(3, ugf.getOdProductCode());
                            ps.setString(4, ugf.getOdSubprodCode());
                            ps.setString(5, ugf.getOdAccNo());
                            ps.setString(6, ugf.getCriteriaType());
                            ps.setString(7, ugf.getUnitId());
                            ps.setString(8, ugf.getOdUsergroupCode());
                            ps.setString(9, ugf.getOdFunctionCode());
                            ps.setString(10, ugf.getOdProductCode());
                            ps.setString(11, ugf.getOdSubprodCode());
                            ps.setString(12, ugf.getOdAccNo());
                            ps.setString(13, ugf.getCriteriaType());
                            ps.addBatch();

                            if ((i + 1) % RECORDS_PER_BATCH == 0 || i == usergpFunctions.size() - 1) {
                                ps.executeBatch();                                
                                records = parseToString(rs, tableName, input, cibut_connection);                                
                            }
                        }
                        
                        ps.close();
                        log.info("[{}][{}/{}]|Batch Insert Successful : {} record/s inserted.",tableName, input.getCif(), input.getCorpCd(), usergpFunctions.size());

                    } catch(BatchUpdateException e) {  
                        
                        log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to " + e.getMessage().trim(), tableName, usergpFunctions.size());                            
                        
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                        throw new CustomException(e.getMessage(), e);
                    }
                } else log.info("[{}]}[{}/{}]|No records to process.",tableName, input.getCif(), input.getCorpCd());
                break;
            case OD_USERGROUP_MB:
                List<Usergroup> usergroups = getUsergroups(rs, cibut_connection);
                isEmpty = usergroups.isEmpty();
                if (!isEmpty) {
                    try (
                            PreparedStatement ps = cibut_connection.prepareStatement(
                                    "INSERT INTO OD_USERGROUP_MB (OD_USERGROUP_CODE, OD_GCIF, OD_USERGROUP_NAME, OD_USERGROUP_DESC, OD_MAKER_ID, OD_MAKER_DATE, OD_AUTH_ID, OD_AUTH_DATE, OD_STATUS, OD_MAKER_NAME, OD_AUTH_NAME, OD_USERGROUP_TYPE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
                            )
                    ) {
                        for (int i = 0; i < usergroups.size(); i++) {
                            Usergroup ug = usergroups.get(i);
                            ps.setString(1, ug.getOdUsergroupCode());
                            ps.setString(2, ug.getOdGcif());
                            ps.setString(3, ug.getOdUsergroupName());
                            ps.setString(4, ug.getOdUsergroupDesc());
                            ps.setString(5, ug.getOdMakerId());
                            ps.setDate(6, new java.sql.Date(ug.getOdMakerDate().getTime()));
                            ps.setString(7, ug.getOdAuthId());
                            ps.setDate(8, new java.sql.Date(ug.getOdAuthDate().getTime()));
                            ps.setString(9, ug.getOdStatus());
                            ps.setString(10, ug.getOdMakerName());
                            ps.setString(11, ug.getOdAuthName());
                            ps.setString(12, ug.getOdUsergroupType());
                            ps.addBatch();

                            if ((i + 1) % RECORDS_PER_BATCH == 0 || i == usergroups.size() - 1) {
                                ps.executeBatch();                                
                                records = parseToString(rs, tableName, input, cibut_connection);
                                
                            }
                        }
                        
                        ps.close();
                        log.info("[{}][{}/{}]|Batch Insert Successful : {} record/s inserted.",tableName, input.getCif(), input.getCorpCd(), usergroups.size());                                

                    } catch(BatchUpdateException e) {  
                        
                        log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to " + e.getMessage().trim(), tableName, usergroups.size());                            
                        
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                        throw new CustomException(e.getMessage(), e);
                    }
                } else log.info("[{}][{}/{}]|No records to process.", tableName, input.getCif(), input.getCorpCd());
                break;
            case OD_USERGP_LIMITS_MB:
                List<UsergpLimit> usergpLimits = getUsergpLimits(rs, cibut_connection);
                isEmpty = usergpLimits.isEmpty();
                if (!isEmpty) {
                    try (
                            PreparedStatement ps = cibut_connection.prepareStatement(
                                    "INSERT INTO OD_USERGP_LIMITS_MB (OD_GCIF, OD_USERGROUP_CODE, UNIT_ID, UNIT_CCY, OD_DAILY_MAX_AMT, OD_DAILY_MAX_NO_TRANS, OD_DAILY_MAX_AMT_UPL, "
                                            + "OD_DAILY_MAX_NO_TRANS_UPL, OD_DAILY_MAX_AUTH_AMT, OD_SELF_FLAG, OD_SELF_AUTH_AMT, OD_APPROVAL_FLAG, OD_AMT_MASKING_FLAG, DAY_CONSOLIDATED_TXN_NO, "
                                            + "DAY_CONSOLIDATED_TXN_AMT, DAY_CONSOL_MAX_APPROVAL_AMT, DAY_MAX_BULK_TXN_APPROVAL_AMT, MAX_BULK_TXN_APPROVAL_AMT, TXN_MAX_APPR_LIMIT_FLAG, "
                                            + "TXN_MAX_APPROVAL_AMT, DAY_MAX_BULK_APPR_LIMIT_FLAG, TXN_MAX_BULK_APPR_LIMIT_FLAG) "
                                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
                            )
                    ) {
                        for (int i = 0; i < usergpLimits.size(); i++) {
                            UsergpLimit ugl = usergpLimits.get(i);
                            ps.setString(1, ugl.getOdGcif());
                            ps.setString(2, ugl.getOdUsergroupCode());
                            ps.setString(3, ugl.getUnitId());
                            ps.setString(4, ugl.getUnitCcy());
                            ps.setBigDecimal(5, ugl.getOdDailyMaxAmt());
                            ps.setString(6, ugl.getOdDailyMaxNoTrans());
                            ps.setBigDecimal(7, ugl.getOdDailyMaxAmtUpl());
                            ps.setString(8, ugl.getOdDailyMaxNoTransUpl());
                            ps.setBigDecimal(9, ugl.getOdDailyMaxAuthAmt());
                            ps.setString(10, ugl.getOdSelfFlag());
                            ps.setBigDecimal(11, ugl.getOdSeltAuthAmt());
                            ps.setString(12, ugl.getOdApprovalFlag());
                            ps.setString(13, ugl.getOdAmtMaskingFlag());
                            ps.setString(14, ugl.getDayConsolidatedTxnNo());
                            ps.setBigDecimal(15, ugl.getDayConsolidatedTxnAmt());
                            ps.setBigDecimal(16, ugl.getDayConsolMaxApprovalAmt());
                            ps.setBigDecimal(17, ugl.getDayMaxBulkTxnApprovalAmt());
                            ps.setBigDecimal(18, ugl.getMaxBulkTxnApprovalAmt());
                            ps.setString(19, ugl.getTxnMaxApprLimitFlag());
                            ps.setBigDecimal(20, ugl.getTxnMaxApprovalAmt());
                            ps.setString(21, ugl.getDayMaxBulkApprLimitFlag());
                            ps.setString(22, ugl.getTxnMaxBulkApprLimitFlag());
                            ps.addBatch();
                            
                            if ((i + 1) % RECORDS_PER_BATCH == 0 || i == usergpLimits.size() - 1) {
                                ps.executeBatch();                                
                                records = parseToString(rs, tableName, input, cibut_connection);
                                
                            }
                        }
                        
                        ps.close();
                        log.info("[{}][{}/{}]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), usergpLimits.size());

                    } catch(BatchUpdateException e) {  
                        
                        log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to " + e.getMessage().trim(), tableName, usergpLimits.size());                            
                        e.printStackTrace();
                        
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                        throw new CustomException(e.getMessage(), e);
                    }
                } else log.info("[{}][{}/{}]|No records to process.", tableName, input.getCif(), input.getCorpCd());
                break;
            case BENEFICIARY_MAINTENANCE:
                List<BeneficiaryMaintenance> benes = getBene(rs);
                isEmpty = benes.isEmpty();
                if (!isEmpty) {
                    try (
                            PreparedStatement ps = cibut_connection.prepareStatement(
                                    "INSERT INTO BENEFICIARY_MAINTENANCE (REFERENCE_NO, TXN_STATUS, CUST_ID, BUSINESSPRODCODE, BENEACCNO, BENENAME, ALIASNAME, BENEACCTYPE, BENEBANKNM, BENEBRANCHNM, BENEBANKNMINTER, BENEADDRESS1) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
                            )
                    ) {
                        for (int i = 0; i < benes.size(); i++) {
                            BeneficiaryMaintenance bene = benes.get(i);
                            ps.setString(1, bene.getReferenceNo());
                            ps.setString(2, bene.getTxnStatus());
                            ps.setString(3, bene.getCustId());
                            ps.setString(4, bene.getBusinessProdCode());
                            ps.setString(5, bene.getBeneAccNo());
                            ps.setString(6, bene.getBeneName());
                            ps.setString(7, bene.getAliasName());
                            ps.setString(8, bene.getBeneAccType());
                            ps.setString(9, bene.getBeneBankNm());
                            ps.setString(10, bene.getBeneBranchNm());
                            ps.setString(11, bene.getBeneBankNmInter());
                            ps.setString(12, bene.getBeneAddress1());
                            ps.addBatch();

                            if ((i + 1) % RECORDS_PER_BATCH == 0 || i == benes.size() - 1) {
                                ps.executeBatch();                                
                                records = parseToString(rs, tableName, input, cibut_connection);
                                
                            }
                        }
                        
                        ps.close();
                        log.info("[{}][{}/{}]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), benes.size());

                    } catch(BatchUpdateException e) {  
                        
                        log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to " + e.getMessage().trim(), tableName, benes.size());                            
                        
                    } catch (SQLException e) {
                        log.error(e.getMessage(), e);
                        throw new CustomException(e.getMessage(), e);
                    }
                } else log.info("[{}][{}/{}]|No records to process.", tableName, input.getCif(), input.getCorpCd()); 
                break;            
        }        
        
        return records;
    }
    
    Map<String, String> default_entry_cimPaymentBkftDtMb;

    @Override
    public List<String> parseToString(List<Map<String, String>> rs, String tableName, Input input, Connection cibut_connection) throws CustomException {
        List<String> records = new ArrayList<>();

        switch (tableName) {
            case CIM_PAYMENT_BKFT_DT_MB:
                List<CimPaymentBkftDtMb> defaulCimPaymentBkftDtMbtEntries = getCimPaymentBkftDtMbDefaultEntries(rs);
                if(!defaulCimPaymentBkftDtMbtEntries.isEmpty()) {
                    for(CimPaymentBkftDtMb defaultEntry : defaulCimPaymentBkftDtMbtEntries) {
                        records.add(input.getCif() + "|" + defaultEntry.getPaymentProduct() + "|" + defaultEntry.getBackDtTxnsAllowed() + "|" 
                                + defaultEntry.getBackDtDays() + "|" + defaultEntry.getFutureDtTxnsAllowed() + "|" + defaultEntry.getFutureDtDays());
                    }
                }
                break;

            case CIM_PAYMENT_PARAMS_MB:
                List<CimPaymentParamsMb> defaultCimPaymentParamsMbEntries = getCimPaymentParamsMbDefaultEntries(rs);
                if(!defaultCimPaymentParamsMbEntries.isEmpty()) {
                    for(CimPaymentParamsMb defaultEntry : defaultCimPaymentParamsMbEntries) {
                        records.add(input.getCif() + "|" + defaultEntry.getPaymentProduct() + "|" + defaultEntry.getExtCutoffTimeApplicable()+ "|" 
                                + defaultEntry.getExtCutoffTime()+ "|" + defaultEntry.getDebitArrDays()+ "|" + defaultEntry.getDebitBasedOnDate() + "|||"
                                + defaultEntry.getHoldDebitDays() + "|" + defaultEntry.getSplittingAllowed() + "|" + defaultEntry.getSplittingThresholdAmt() + "||" 
                                + defaultEntry.getSlaReq() + "|" + defaultEntry.getSlaMinutes());
                    }
                }
                break;
                
            case CIM_BUSINESS_PARAMS_MB:
                List<CimBusinessParamsMb> defaultCimBusinessParamsMbEntries = getCimBusinessParamsMbsDefaultEntries(rs);
                if(!defaultCimBusinessParamsMbEntries.isEmpty()) {
                    for(CimBusinessParamsMb defaultEntry : defaultCimBusinessParamsMbEntries) {
                        records.add(input.getCif() + "|" + defaultEntry.getBusinessProduct() + "||||" + defaultEntry.getOnlyRegBeneForTxn() + "|||" 
                                + defaultEntry.getDebitLvlId() + "|" + defaultEntry.getBatchRuleId() + "|" + defaultEntry.getBatchRuleName() + "|");
                    }
                }
                break;
                
            case CIM_SUBPROD_ATTR_MAP_MB:
                List<CimSubProdAttrMapMb> defaultCimSubProdAttrMapMbEntries = getCimSubProdAttrMapMbDefaultEntries(rs);
                if(!defaultCimSubProdAttrMapMbEntries.isEmpty()) {
                    for(CimSubProdAttrMapMb defaultEntry : defaultCimSubProdAttrMapMbEntries) {
                        records.add(input.getCif() + "|" + defaultEntry.getSubProduct() + "|" + defaultEntry.getProduct() + "|" + defaultEntry.getAttrLevel() + "|"
                                + defaultEntry.getType() + "|" + defaultEntry.getSubProductName());
                    }
                }
                break;
                
            case OD_CORPORATE_FUNCTION_MB:
                List<CorporateFunction> corpFunctions = getCorporateFunctions(rs);
                if (!corpFunctions.isEmpty()) {
                    for (CorporateFunction c : corpFunctions) {
                        String line = input.getCorpCd() + "|" + c.getOdFunctionCode() + "|" + c.getOdProductCode()
                                + "|" + c.getOdSubprodCode() + "|" + c.getVerificationReq()
                                + "|" + c.getReleaseReq();
                        records.add(line);
                    }
                }
                break;            
                
            case OD_USERGP_FUNCTION_MB:
                List<UsergpFunction> usergpFunctions = getUsergpFunctions(input, rs, cibut_connection);
                if (!usergpFunctions.isEmpty()) {
                    for (UsergpFunction ugf : usergpFunctions) {
                        String line = ugf.getOdUsergroupCode() + "|" + ugf.getOdFunctionCode()
                                + "|" + ugf.getOdProductCode() + "|" + ugf.getOdSubprodCode() + "|" + ugf.getOdAccNo()
                                + "|" + "|" + ugf.getCriteriaType() + "|" + ugf.getUnitId();
                        records.add(line);
                    }
                }
                break;
            case OD_USERGROUP_MB:
                List<Usergroup> usergroups = getUsergroups(rs, cibut_connection);                
                if (!usergroups.isEmpty()) {
                    for (Usergroup ug : usergroups) {
                        String line = ug.getOdUsergroupCode() + "|" + ug.getOdGcif() + "|" + ug.getOdUsergroupName()
                                + "|" + ug.getOdUsergroupDesc() + "|" + ug.getOdMakerId() + "|" + toStringDate(ug.getOdMakerDate())
                                + "|" + ug.getOdAuthId() + "|" + toStringDate(ug.getOdAuthDate()) + "|" + ug.getOdStatus()
                                + "|" + ug.getOdMakerName() + "|" + ug.getOdAuthName() + "|" + ug.getOdUsergroupType() + "|||";
                        records.add(line);
                    }
                }
                break;
            case OD_USERGP_LIMITS_MB:
                List<UsergpLimit> usergpLimits = getUsergpLimits(rs, cibut_connection);
                if (!usergpLimits.isEmpty()) {
                    for (UsergpLimit ugl : usergpLimits) {
                        String line = ugl.getOdGcif() + "|" + ugl.getOdUsergroupCode() + "|" + ugl.getUnitId()
                                + "|" + ugl.getUnitCcy() + "|" + ugl.getOdDailyMaxAmt() +"|"+ ugl.getOdDailyMaxNoTrans() +"|" + ugl.getOdDailyMaxAmtUpl() 
                                + "|" + ugl.getOdDailyMaxNoTransUpl() + "|" + ugl.getOdDailyMaxAuthAmt() + "|" + ugl.getOdSelfFlag()
                                + "|" + ugl.getOdSeltAuthAmt() + "|" + ugl.getOdApprovalFlag() + "|" + ugl.getOdProductCode() + "|" + ugl.getOdSubprodCode()
                                + "|" + ugl.getOdTransMaxAmt() + "|||" + ugl.getOdAmtMaskingFlag() + "|" + ugl.getDayConsolidatedTxnNo() 
                                + "|" + ugl.getDayConsolidatedTxnAmt() + "|" + ugl.getDayConsolMaxApprovalAmt() + "|" + ugl.getDayMaxBulkTxnApprovalAmt() 
                                + "|" + ugl.getMaxBulkTxnApprovalAmt() + "|" + ugl.getTxnMaxApprLimitFlag() + "|" + ugl.getTxnMaxApprovalAmt() 
                                + "|" + ugl.getDayMaxBulkApprLimitFlag() + "|" + ugl.getTxnMaxBulkApprLimitFlag();
                        records.add(line);
                    }
                }
                break;
            case BENEFICIARY_MAINTENANCE:
                List<BeneficiaryMaintenance> benes = getBene(rs);
                if (!benes.isEmpty()) {
                    for (BeneficiaryMaintenance bene : benes) {
                        String line = bene.getReferenceNo() + "|" + bene.getTxnStatus() + "|" + bene.getCustId()
                                + "|" + bene.getBusinessProdCode() + "|" + bene.getBeneAccNo() + "||" + bene.getBeneName()
                                + "||" + bene.getAliasName() + "|" + bene.getBeneAccType() + "|" + bene.getBeneBankNm()
                                + "|" + bene.getBeneBranchNm() + "|||" + bene.getBeneBankNmInter() + "|||||" + bene.getBeneAddress1() + "|||||||||||||||||||||||||||||||||||||||||||||||||||||||||";
                        records.add(line);
                    }
                }
        }

        return records;
    }    
    
    private static List<Integer> rulesIdList =  new ArrayList<>();
    private static List<Integer> NoWorkflowRuleIds = new ArrayList<>();

    @Override
    public List<String> rulesParsing(List<Map<String, String>> rs, String tableName, Input input, Connection cib_connection, Connection cibut_connection) throws CustomException {
        
    	int randomId;
    	Random random = new Random();
        
        while (true) {
        	randomId = random.nextInt(2000000000);
        	if (!rulesIdList.contains(Integer.valueOf(randomId))) {
        		rulesIdList.add(randomId);
        		break;
        	}
        }

        Rules rules = getRules(rs, input, randomId, randomId, cib_connection);

        List<String> allRecords = new ArrayList<>();
        Map<Integer, Rules> rule_id_map = new HashMap<>();
        List<Integer> parsed_rule_list = new ArrayList<>();

        try {
//            ----------------- RULES -----------------
            PreparedStatement rulesPs = cibut_connection.prepareStatement(
                    "INSERT INTO OD_RULES_MB (OD_GCIF, OD_RULE_ID, OD_RULE_NAME, OD_RULE_DESC, OD_MAKER_ID, OD_MAKER_DATE, OD_AUTH_ID, OD_AUTH_DATE, OD_STATUS, OD_MAKER_NAME, OD_AUTH_NAME, APPROVAL_MODE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
            );
            int ruleListSize = rules.getRuleList().size();
            
            if(ruleListSize == 0)
                log.info("[{}][{}/{}]|[OD_RULES_MB]|No records to process.", tableName, input.getCif(), input.getCorpCd());
            
            Map<String, String> rule_name_map = new HashMap<>();
            
            for (int i = 0; i < ruleListSize; i++) {
                Rule rule = rules.getRuleList().get(i);
                String rule_name = rule.getOdRuleName();
                
                String odStatus = "";
                String approvalMode = "";
            	
            	if(NoWorkflowRuleIds.contains(Integer.valueOf(rule.getOdRuleId()))) {
            		odStatus = "A";
            		approvalMode = "FLA";
            	}
                
                if(rule_name_map.get(rule_name) == null) {
                    String line = rule.getOdGcif() + "|" + rule.getOdRuleId() + "|" + rule.getOdRuleName()
                            + "|" + rule.getOdRuleDesc() + "|MIGMKR|" + toStringDate(new Date()) + "|MIGAUTH|"
                            + toStringDate(new Date()) + "|" + odStatus + "|MIGMKR|MIGAUTH|||" + approvalMode + "||||rule";
                    allRecords.add(line);

                    rulesPs.setString(1, rule.getOdGcif());
                    rulesPs.setInt(2, rule.getOdRuleId());
                    rulesPs.setString(3, rule.getOdRuleName());
                    rulesPs.setString(4, rule.getOdRuleDesc());
                    rulesPs.setString(5, "MIGMKR");
                    java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
                    rulesPs.setDate(6, sqlDate);
                    rulesPs.setString(7, "MIGAUTH");
                    rulesPs.setDate(8, sqlDate);
                    rulesPs.setString(9, odStatus);
                    rulesPs.setString(10, "MIGMKR");
                    rulesPs.setString(11, "MIGAUTH");
                    rulesPs.setString(12, approvalMode);
                    rulesPs.addBatch();
                    rule_name_map.put(rule_name, "");
                    rule_id_map.put(rule.getOdRuleId(), rules);
                    
                }

                if ((i + 1) % RECORDS_PER_BATCH == 0 || i == ruleListSize - 1) {

                    rulesPs.executeBatch();

                }
                
            }
            rulesPs.close();
            log.info("[{}][{}/{}]|[OD_RULES_MB]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), rule_name_map.size());
//            ----------------- RULES -----------------

//            ----------------- RULES DEF -----------------
            PreparedStatement rulesDefPs = cibut_connection.prepareStatement(
                    "INSERT INTO OD_RULES_DEF_MB (OD_RULE_ID, OD_RULE_PARSE_ID, OD_MIN_AMT, OD_MAX_AMT, RULE_INFO) VALUES (?,?,?,?,?)"
            );
            int rulesDefSize = rules.getRulesDefList().size();
            
            if(rulesDefSize == 0)
                log.info("[{}][{}/{}]|[OD_RULES_DEF_MB]|No records to process.", tableName, input.getCif(), input.getCorpCd());
                        
            for (int j = 0; j < rulesDefSize; j++) {
                                
                RulesDef rulesDef = rules.getRulesDefList().get(j);
//                if(rule_id_map.get(rulesDef.getOdRuleId()) != null) {
                    
                    String line = rulesDef.getOdRuleId() + "|" + rulesDef.getOdRuleParseId() + "|" + rulesDef.getOdMinAmt()
                            + "|" + rulesDef.getOdMaxAmt() + "|" + rulesDef.getRuleInfo() + "rulesDef";
                    allRecords.add(line);

                    rulesDefPs.setInt(1, rulesDef.getOdRuleId());
                    rulesDefPs.setInt(2, rulesDef.getOdRuleParseId());
                    rulesDefPs.setBigDecimal(3, rulesDef.getOdMinAmt());
                    rulesDefPs.setBigDecimal(4, rulesDef.getOdMaxAmt());
                    rulesDefPs.setString(5, rulesDef.getRuleInfo());
                    rulesDefPs.addBatch();
                    parsed_rule_list.add(rulesDef.getOdRuleParseId());
//                }

                if ((j + 1) % RECORDS_PER_BATCH == 0 || j == rulesDefSize - 1) {
                    rulesDefPs.executeBatch();
                    
                }

            }
            rulesDefPs.close();
            log.info("[{}][{}/{}]|[OD_RULES_DEF_MB]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), rule_id_map.size());
//            ----------------- RULES DEF -----------------

//            ----------------- PARSED RULE -----------------
            PreparedStatement parsedPs = cibut_connection.prepareStatement(
                    "INSERT INTO OD_PARSED_RULE_MB (OD_RULE_PARSE_ID, OD_LEVEL, OD_COUNT, APPROVAL_FLOW) VALUES (?,?,?,?)"
            );
            int parsedSize = rules.getParsedRuleList().size();
            
            if(parsedSize == 0)
                log.info("[{}][{}/{}]|[OD_PARSED_RULE_MB]|No records to process.", tableName, input.getCif(), input.getCorpCd());
                        
            for (int k = 0; k < parsedSize; k++) {
                
                
                ParsedRule parsedRule = rules.getParsedRuleList().get(k);
//                if(parsed_rule_list.contains(parsedRule.getOdRuleParseId())){
                    String line = parsedRule.getOdRuleParseId() + "|" + parsedRule.getOdLevel() +
                            "|" + parsedRule.getOdCount() + "|" + parsedRule.getApprovalFlow() + "parsedRule";
                    allRecords.add(line);

                    parsedPs.setInt(1, parsedRule.getOdRuleParseId());
                    parsedPs.setString(2, parsedRule.getOdLevel());
                    parsedPs.setInt(3, parsedRule.getOdCount());
                    parsedPs.setString(4, parsedRule.getApprovalFlow());
                    parsedPs.addBatch();

                    if ((k + 1) % RECORDS_PER_BATCH == 0 || k == parsedSize - 1) {
                        parsedPs.executeBatch();

                    }
//                }
            }
            parsedPs.close();
            log.info("[{}][{}/{}]|[OD_PARSED_RULE_MB]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), parsedSize);
//            ----------------- PARSED RULE -----------------

//            ----------------- RULES ACC MAP -----------------
            PreparedStatement rulesAccPs = cibut_connection.prepareStatement(
                    "INSERT INTO OD_RULES_ACC_MAP_MB (OD_RULE_ID, OD_FUNCTION_ID, OD_PRODUCT_CODE, OD_SUBPRODUCT_CODE, OD_ORG_ACC_NO, OD_GCIF, CRITERIA_TYPE, CURRENCY, UNIT_ID) VALUES (?,?,?,?,?,?,?,?,?)"
            );
            int rulesAccSize = rules.getRulesAccMapList().size();
            
            if(rulesAccSize == 0)
                log.info("[{}][{}/{}]|[OD_RULES_ACC_MAP_MB]|No records to process.", tableName, input.getCif(), input.getCorpCd());
                        
            for (int l = 0; l < rulesAccSize; l++) {
                
                RulesAccMap rulesAccMap = rules.getRulesAccMapList().get(l);
                if(rule_id_map.get(rulesAccMap.getOdRuleId()) != null) {
                    String line = rulesAccMap.getOdRuleId() + "|" + rulesAccMap.getOdFunctionId() + "|" + rulesAccMap.getOdProductCode()
                            + "|" + rulesAccMap.getOdSubproductCode() + "|" + rulesAccMap.getOdGcif() + "|" 
                            + rulesAccMap.getOdOrgAccNo() + "|" + rulesAccMap.getCriteriaType() + "|"
                            + rulesAccMap.getCurrency() + "|" + rulesAccMap.getUnitId() + "rulesAcc";
                    allRecords.add(line);

                    rulesAccPs.setInt(1, rulesAccMap.getOdRuleId());
                    rulesAccPs.setString(2, rulesAccMap.getOdFunctionId());
                    rulesAccPs.setString(3, rulesAccMap.getOdProductCode());
                    rulesAccPs.setString(4, rulesAccMap.getOdSubproductCode());
                    rulesAccPs.setString(5, rulesAccMap.getOdOrgAccNo());
                    rulesAccPs.setString(6, rulesAccMap.getOdGcif());
                    rulesAccPs.setString(7, rulesAccMap.getCriteriaType());
                    rulesAccPs.setString(8, rulesAccMap.getCurrency());
                    rulesAccPs.setString(9, rulesAccMap.getUnitId());
                    rulesAccPs.addBatch();
                } 
                if ((l + 1) % RECORDS_PER_BATCH == 0 || l == rulesAccSize - 1) {
                    rulesAccPs.executeBatch();
                    
                }
            }
            rulesAccPs.close();
            log.info("[{}][{}/{}]|[OD_RULES_ACC_MAP_MB]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), rulesAccSize);
//            ----------------- RULES ACC MAP -----------------                        
            

        }  catch (BatchUpdateException e) {
            
            log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : " + e.getMessage().trim(), tableName);                            
            
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return allRecords;
    }
    
    public static final Map<String, UserRoleMap> userRoleMap = new HashMap<>();

    @Override
    public List<String> rolesParsing(List<Map<String, String>> rs, String tableName, Input input, Connection cib_connection, Connection cibut_connection) throws CustomException {
        List<Role> roles = getRoles(rs, input);
        List<UserRoleContainer> userRoleContainers = new ArrayList<>();
        List<UserRoleMap> userRoleMaps = new ArrayList<>();
        List<String> records = new ArrayList<>();

        try (               
               PreparedStatement ps = cib_connection.prepareStatement(qcfg.gnrtSelectUserRoles)
        ) {
            ps.setString(1, input.getCorpCd());

            ResultSet rs1 = ps.executeQuery();

            while (rs1.next()) {
            	String od_user_no = rs1.getString("CD").replaceAll("\\s",  ""); //remove whitespace characters
            	od_user_no = resultSetParser.replaceSpecialCharacters(od_user_no, dmService.getSpecialCharacters(), true);// filter special characters from OD_USER_ROLES_MAP_MB.OD_USER_NO
            	od_user_no = od_user_no.substring(0, Math.min(od_user_no.length(), 20)); //limit string length to 20
            	if(od_user_no.length() < 8) //Pad OD_USER_ROLES_MAP_MB.OD_USER_NO with 'x' if less than 8 in length
            		od_user_no = resultSetParserService.padLoginId(od_user_no);
            	
            	userRoleContainers.add(
                        UserRoleContainer.builder()
                                .corpGroupId(rs1.getString("CORP_GROUP_ID"))
                                .userCd(od_user_no)
                                .role(rs1.getInt("ROLE"))
                                .roleDesc(rs1.getString("ROLES"))
                                .levelClass(rs1.getString("CLASS"))
                                .build()
                );
            }
            
            rs1.close();
            ps.close();            

        } catch (BatchUpdateException e) {
            
            log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : " + e.getMessage().trim(), tableName);                            
            
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CustomException(e.getMessage(), e);
        }

        for (UserRoleContainer urc : userRoleContainers) {
            String query = "SELECT CMS_CG_ID FROM CORP_GROUP_ID_CONVERT WHERE BOB_CG_ID = '" + urc.getCorpGroupId() + "'";
            try (                    
                    PreparedStatement stmt = cibut_connection.prepareStatement(query);
                    ResultSet result = stmt.executeQuery();
            ) {
                while (result.next()) {
                    urc.setCorpGroupId(result.getString("CMS_CG_ID"));
                }
                
                result.close();
                stmt.close();                

            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new CustomException(e.getMessage(), e);
            }


            String roleDesc;            
            switch (urc.getRole()) {
                case 0:
                    roleDesc = "MAKER";
                    break;
                case 1:
                    roleDesc = urc.getLevelClass() + " VERIFIER";
                    break;
                case 2:
                    roleDesc = urc.getLevelClass() + " AUTHORIZER";
                    break;
                case 3:
                    roleDesc = urc.getLevelClass() + " RELEASER";
                    break;
                case 4:
                    roleDesc = "VIEWER";
                    break;              
                default:
                    roleDesc = "CORPADMIN";
            }
            String roleLevel = null;
            if (roleDesc.equals("CORPADMIN"))
                    roleLevel = "A" + (roles.size() + 11) + "ALL";
            else {                
                for (Role role : roles) {                    
                    if (roleDesc.replaceFirst("null ", "").equals(role.getOdRoleDesc())){
                        roleLevel = role.getOdLevel();
                    }                                
                }
            }
            
            if (roleLevel != null) {
                userRoleMaps.add(
                        UserRoleMap.builder()
                                .odUserNo(urc.getUserCd())
                                .odGcif(input.getCorpCd())
                                .odRoleLevel(roleLevel)
                                .odUsergroupCode(urc.getCorpGroupId())
                                .unitId("ALL")
                                .role(urc.getRole())
                                .build()
                );
            }

        }

        try {
            List<Role> filteredRoles = roles.stream()
                    .filter(distinctByKey(r -> r.getOdGcif()+r.getOdLevel()))
                    .collect(Collectors.toList());

            PreparedStatement rolePs = cibut_connection.prepareStatement(
                    "INSERT INTO OD_ROLES_MB (OD_GCIF, OD_LEVEL, OD_ROLE_DESC, OD_MAKER_ID, OD_MAKER_DATE, OD_AUTH_ID, OD_AUTH_DATE, OD_MAKER_NAME, OD_AUTH_NAME, UNIT_ID) VALUES (?,?,?,?,?,?,?,?,?,?)"
            );
            int rolesSize = filteredRoles.size();
            List<String> roles_records = new ArrayList<>();
            for (int i = 0, roleIndex = 11; i < rolesSize; i++, roleIndex++) {
                Role role = filteredRoles.get(i);
                String line = role.getOdGcif() + "|" + role.getOdLevel() + "|" + role.getOdRoleDesc()
                        + "|MIGMKR|" + toStringDate(new Date()) + "|MIGAUTH|" + toStringDate(new Date())
                        + "||MIGMKR|MIGAUTH|ALL|||role";
                roles_records.add(line);

                rolePs.setString(1, role.getOdGcif());
                rolePs.setString(2, role.getOdLevel());
                rolePs.setString(3, role.getOdRoleDesc());
                rolePs.setString(4, "MIGMKR");
                java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
                rolePs.setDate(5, sqlDate);
                rolePs.setString(6, "MIGAUTH");
                rolePs.setDate(7, sqlDate);
                rolePs.setString(8, "MIGMKR");
                rolePs.setString(9, "MIGAUTH");
                rolePs.setString(10, "ALL");
                rolePs.addBatch();

                if ((i + 1) % RECORDS_PER_BATCH == 0 || i == rolesSize - 1) {
                    
                    //default entry for CORPADMIN
                    if(i == rolesSize - 1)
                    {   
                        line = input.getCorpCd() + "|A" + ++roleIndex + "ALL|CORPADMIN|MIGMKR|" + toStringDate(new Date()) + "|MIGAUTH|" + toStringDate(new Date()) + "||MIGMKR|MIGAUTH|ALL|||role";
                        roles_records.add(line);
                        rolePs.setString(1, role.getOdGcif());
                        rolePs.setString(2, "A" + roleIndex++ + "ALL");
                        rolePs.setString(3, "CORPADMIN");
                        rolePs.setString(4, "MIGMKR");
                        rolePs.setDate(5, sqlDate);
                        rolePs.setString(6, "MIGAUTH");
                        rolePs.setDate(7, sqlDate);
                        rolePs.setString(8, "MIGMKR");
                        rolePs.setString(9, "MIGAUTH");
                        rolePs.setString(10, "ALL");
                        rolePs.addBatch();
                        
                    }
                    
                    rolePs.executeBatch();
                    records.addAll(roles_records);
                    
                    
                    
                }

            }
            
            rolePs.close();
            log.info("[{}][{}/{}]|[OD_ROLES_MB]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), rolesSize);

            List<UserRoleMap> filteredUrms = userRoleMaps.stream()
                    .filter(distinctByKey(urm -> urm.getOdUserNo()+urm.getOdGcif()+urm.getOdRoleLevel()))
                    .collect(Collectors.toList());

            int roleMapSize = filteredUrms.size();
            List<String> roles_map_records = new ArrayList<>();
            for (int j = 0; j < roleMapSize; j++) {     
                UserRoleMap urm = filteredUrms.get(j);
                
                String login_id = urm.getOdUserNo();
                login_id = login_id.replaceAll("\\s",  ""); //remove whitespace characters
                login_id = resultSetParser.replaceSpecialCharacters(login_id, dmService.getSpecialCharacters(), true);
                login_id = login_id.substring(0, Math.min(login_id.length(), 20)); //limit string length to 20
                if(login_id.length() < 8)
                	login_id = resultSetParserService.padLoginId(login_id);
                String org_id = GenerateServiceImpl.get_org_id_mapping(cfg.inputPath).getOrDefault(input.getCorpCd(), new String[]{"", input.getCorpCd()})[0];
                
                if(org_id == null || org_id.isEmpty())
                        org_id = input.getCorpCd();
                String domain_id = urm.getOdGcif();
                String key = login_id + "-" + org_id;
                
                BobUser bob_user = GenerateServiceImpl.bob_users.get(key);
                String default_domain = null;
                
                if(bob_user != null)
                        default_domain= bob_user.getDomain_id();
                
                if(default_domain != null)
                    domain_id = default_domain;
                
                urm.setOdGcif(default_domain);
                String userRoleKey = urm.getOdUserNo() + "|" + default_domain;
                UserRoleMap rolemap = userRoleMap.get(userRoleKey);
                if(rolemap == null){
                                        
                    userRoleMap.put(userRoleKey, urm);
                    
                } else {
                    
                    int urm_role = urm.getRole();
                    int map_role = rolemap.getRole();
                    
                    if(urm_role > map_role)
                        userRoleMap.put(userRoleKey, urm);
                    
                }                
                
            }                        
         
            log.info("[{}][{}/{}]|[OD_USER_ROLES_MAP_MB]|Batch Insert Successful : {} record/s inserted.", tableName, input.getCif(), input.getCorpCd(), roleMapSize);

        } catch (BatchUpdateException e) {
            
            log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : " + e.getMessage().trim(), tableName);                            
            
        } catch (SQLException | GenerateException e) {
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|An exception has been encountered : \n{}" + e.getMessage().trim(), tableName, sw.toString());            
            
        }


        return records;
    }


    private List<BeneficiaryMaintenance> getBene(List<Map<String, String>> rs) {
        List<BeneficiaryMaintenance> benes = new ArrayList<>();
        for(Map<String, String> map : rs) {
            benes.add(
                    BeneficiaryMaintenance.builder()
                            .referenceNo(map.get("REFERENCE_NO"))
                            .txnStatus(map.get("TXN_STATUS"))
                            .custId(map.get("CUST_ID"))
                            .businessProdCode(map.get("BUSINESSPRODCODE"))
                            .beneAccNo(map.get("BENEACCNO"))
                            .beneName(map.get("BENENAME"))
                            .aliasName(map.get("ALIASNAME"))
                            .beneAccType(map.get("BENEACCTYPE"))
                            .beneBankNm(map.get("BENEBANKNM"))
                            .beneBranchNm(map.get("BENEBRANCHNM"))
                            .beneBankNmInter(map.get("BENEBANKNMINTER"))
                            .beneAddress1(map.get("BENEADDRESS1"))
                            .build()
            );
        }

        return benes.stream()
                .filter(distinctByKey(BeneficiaryMaintenance::getReferenceNo))
                .collect(Collectors.toList());
    }

   
    private static List<CorporateFunction> defaultCorporateFunctions;
    private List<CorporateFunction> getDefaultCorporateFunctions() throws CustomException {
        
        if(defaultCorporateFunctions == null) {
            
            defaultCorporateFunctions = new ArrayList<>();
            File file = new File("config/product_codes_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String defaultFunction;
                while ((defaultFunction = br.readLine()) != null) {
                   String[] details = defaultFunction.split(",");
                   defaultCorporateFunctions.add(CorporateFunction.builder()
                           .odProductCode(details[0])
                           .odSubprodCode(details[1])
                           .odFunctionCode(details[2])
                           .verificationReq(details[3])
                           .releaseReq(details[4])
                           .build()
                   );
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
                        
        }
        
        return defaultCorporateFunctions;
    }
    
    private static Map<String, List<CorporateFunction>> paymnt_subprod_codes;
    private Map<String, List<CorporateFunction>> getPaymntSubProdCodes() throws CustomException{
        
        if(paymnt_subprod_codes == null)
        {
            paymnt_subprod_codes = new HashMap<>();
            File file = new File("config/paymnt_subprod_codes_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String subProdCode;
                while ((subProdCode = br.readLine()) != null) 
                {                    
                    String[] details = subProdCode.split(",");    
                    List<CorporateFunction> subprodcode = paymnt_subprod_codes.get(details[0]);                    
                    if(subprodcode == null)
                    {
                        subprodcode = new ArrayList<>();
                                                      
                    } 
                    
                    subprodcode.add(CorporateFunction.builder()
                                .odProductCode("PAYMNT")
                                .odSubprodCode(details[0])
                                .odFunctionCode(details[1])
                                .verificationReq(details[2])
                                .releaseReq(details[3])
                                .build()
                    );
                    
                    paymnt_subprod_codes.put(details[0], subprodcode);  
                   
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
        }
        return paymnt_subprod_codes;
        
    }
    
    private List<CorporateFunction> addCorporateFunctions(List<CorporateFunction> corp_functions, String req, String rel_req_flag)
    {
        List<CorporateFunction> corp_functions_list = new ArrayList<>();
        for(CorporateFunction corp_function : corp_functions)
        {         
            String ver_req;
            String rel_req;
            
            ver_req = corp_function.getVerificationReq();
            rel_req = corp_function.getReleaseReq();
            
            if(!req.equalsIgnoreCase("BENE"))
            {

                if(corp_function.getVerificationReq().equalsIgnoreCase("N"))
                {
                    ver_req = req;
                }

                if(corp_function.getReleaseReq().equalsIgnoreCase("N"))
                {
                    rel_req = rel_req_flag;
                }
            }

            corp_functions_list.add(CorporateFunction.builder()                            
                    .odProductCode(corp_function.getOdProductCode())
                    .odSubprodCode(corp_function.getOdSubprodCode())
                    .odFunctionCode(corp_function.getOdFunctionCode())
                    .releaseReq(rel_req)
                    .verificationReq(ver_req)
                    .build()                            
            );
        }
        return corp_functions_list;
    }
    
    private static final Map<String, List<CimPaymentBkftDtMb>> cimPaymentBkftDtMbMap = new HashMap<>();
    private static final Map<String, List<CimPaymentParamsMb>> cimPaymentParamsMbMap = new HashMap<>();
    private static final Map<String, List<CimBusinessParamsMb>> cimBusinessParamsMbMap = new HashMap<>();
    private static final Map<String, List<CimSubProdAttrMapMb>> cimSubProdAttrMapMb = new HashMap<>();
    
    private static Map<String, List<CimPaymentBkftDtMb>> getCimPaymentBkftDtMbMapping() throws CustomException {
        
        if(cimPaymentBkftDtMbMap.isEmpty()) {
            
            File file = new File("config/tbl05_subproducts_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    
                    String[] details = line.split(",");         
                    CimPaymentBkftDtMb defaultMapping = CimPaymentBkftDtMb.builder()
                            .backDtDays(details[3])
                            .backDtTxnsAllowed(details[2])
                            .futureDtDays(details[5])
                            .futureDtTxnsAllowed(details[4])
                            .paymentProduct(details[1])
                            .build();
                    if(cimPaymentBkftDtMbMap.containsKey(details[0]))
                        cimPaymentBkftDtMbMap.get(details[0]).add(defaultMapping);
                    else {
                        List<CimPaymentBkftDtMb> defaultMappingList = new ArrayList<>();
                        defaultMappingList.add(defaultMapping);
                        cimPaymentBkftDtMbMap.put(details[0], defaultMappingList);
                    }
                    
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
            
        }
        return cimPaymentBkftDtMbMap;
    }
    
    private static Map<String, List<CimPaymentParamsMb>> getCimPaymentParamsMbMapping() throws CustomException {
        
        if(cimPaymentParamsMbMap.isEmpty()) {
            
            File file = new File("config/tbl07_subproducts_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    
                    String[] details = line.split(",");         
                    CimPaymentParamsMb defaultMapping = CimPaymentParamsMb.builder()
                            .debitArrDays(details[4])
                            .debitBasedOnDate(details[5])
                            .extCutoffTime(details[3])
                            .extCutoffTimeApplicable(details[2])
                            .holdDebitDays(details[6])
                            .paymentProduct(details[1])
                            .slaMinutes(details[10])
                            .slaReq(details[9])
                            .splittingAllowed(details[7])
                            .splittingThresholdAmt(details[8])                            
                            .build();
                    if(cimPaymentParamsMbMap.containsKey(details[0]))
                        cimPaymentParamsMbMap.get(details[0]).add(defaultMapping);
                    else {
                        List<CimPaymentParamsMb> defaultMappingList = new ArrayList<>();
                        defaultMappingList.add(defaultMapping);
                        cimPaymentParamsMbMap.put(details[0], defaultMappingList);
                    }
                    
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
            
        }
        return cimPaymentParamsMbMap;
    }
    
    private static Map<String, List<CimBusinessParamsMb>> getCimBusinessParamsMbMapping() throws CustomException {
        
        if(cimBusinessParamsMbMap.isEmpty()) {
            
            File file = new File("config/tbl08_subproducts_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    
                    String[] details = line.split(",");         
                    CimBusinessParamsMb defaultMapping = CimBusinessParamsMb.builder()
                            .batchRuleId(details.length >= 5 ? details[4] : "")
                            .businessProduct(details[1])
                            .debitLvlId(details[3])
                            .onlyRegBeneForTxn(details[2])
                            .batchRuleName(details.length >= 6 ? details[5] : "")
                            .build();
                    if(cimBusinessParamsMbMap.containsKey(details[0]))
                        cimBusinessParamsMbMap.get(details[0]).add(defaultMapping);
                    else {
                        List<CimBusinessParamsMb> defaultMappingList = new ArrayList<>();
                        defaultMappingList.add(defaultMapping);
                        cimBusinessParamsMbMap.put(details[0], defaultMappingList);
                    }
                    
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
            
        }
        return cimBusinessParamsMbMap;
    }
    
    private static Map<String, List<CimSubProdAttrMapMb>> getCimSubProdAttrMapMbMapping() throws CustomException {
        
        if(cimSubProdAttrMapMb.isEmpty()) {
            
            File file = new File("config/tbl29_subproducts_mapping.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String line;
                while ((line = br.readLine()) != null) {
                    
                    String[] details = line.split(",");
                    CimSubProdAttrMapMb defaultMapping = CimSubProdAttrMapMb.builder()
                            .attrLevel(details[5])
                            .product(details[1])
                            .subProduct(details[2])
                            .subProductName(details[3])
                            .type(details[4])
                            .build();
                    
                    if(cimSubProdAttrMapMb.containsKey(details[0]))
                        cimSubProdAttrMapMb.get(details[0]).add(defaultMapping);
                    else {
                        List<CimSubProdAttrMapMb> defaultMappingList = new ArrayList<>();
                        defaultMappingList.add(defaultMapping);
                        cimSubProdAttrMapMb.put(details[0], defaultMappingList);
                    }
                    
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
            
        }
        return cimSubProdAttrMapMb;        
    }
    
    private List<CimPaymentBkftDtMb> getCimPaymentBkftDtMbDefaultEntries(List<Map<String, String>> rs) throws CustomException {
        
        List<CimPaymentBkftDtMb> defaultEntries = new ArrayList<>();        
        
        for(Map<String, String> map : rs) {
            String functionId = map.get("OD_SUBPROD_CODE");            
            List<String> paymntsubprodcodes = getPaymntSubProdCodes(functionId);
            for(String subProdCode : paymntsubprodcodes) {
                List<CimPaymentBkftDtMb> entries = getCimPaymentBkftDtMbMapping().get(subProdCode);
                if(entries != null)
                    defaultEntries.addAll(entries);
            }
        }
        
        return defaultEntries.stream()
                .filter(distinctByKey(f -> f.getPaymentProduct()))
                .collect(Collectors.toList());
        
    }
    
    private List<CimPaymentParamsMb> getCimPaymentParamsMbDefaultEntries(List<Map<String, String>> rs) throws CustomException {
        
        List<CimPaymentParamsMb> defaultEntries = new ArrayList<>();        
        
        for(Map<String, String> map : rs) {
            String functionId = map.get("OD_SUBPROD_CODE");            
            List<String> paymntsubprodcodes = getPaymntSubProdCodes(functionId);            
            for(String subProdCode : paymntsubprodcodes) {
                List<CimPaymentParamsMb> entries = getCimPaymentParamsMbMapping().get(subProdCode);
                if(entries != null)
                    defaultEntries.addAll(entries);
            }
        }
        
        return defaultEntries.stream()
                .filter(distinctByKey(f -> f.getPaymentProduct()))
                .collect(Collectors.toList());
        
    }
    
    private List<CimBusinessParamsMb> getCimBusinessParamsMbsDefaultEntries(List<Map<String, String>> rs) throws CustomException {
        
        List<CimBusinessParamsMb> defaultEntries = new ArrayList<>();
        
        for(Map<String, String> map : rs) {
            String functionId = map.get("OD_SUBPROD_CODE");
            List<String> paymntsubprodcodes = getPaymntSubProdCodes(functionId);            
            for(String subProdCode : paymntsubprodcodes) {
                List<CimBusinessParamsMb> entries = getCimBusinessParamsMbMapping().get(subProdCode);
                if(entries != null)
                    defaultEntries.addAll(entries);
            }
        }
        
        return defaultEntries.stream()
                .filter(distinctByKey(f -> f.getBusinessProduct()))
                .collect(Collectors.toList());
        
    }
    
    private List<CimSubProdAttrMapMb> getCimSubProdAttrMapMbDefaultEntries(List<Map<String, String>> rs) throws CustomException {
        
        List<CimSubProdAttrMapMb> defaultEntries = new ArrayList<>();
        
        for(Map<String, String> map : rs) {
            String functionId = map.get("OD_SUBPROD_CODE");
            List<String> paymntsubprodcodes = getPaymntSubProdCodes(functionId);
            for(String subProdCode : paymntsubprodcodes) {
                List<CimSubProdAttrMapMb> entries = getCimSubProdAttrMapMbMapping().get(subProdCode);
                if(entries != null)
                    defaultEntries.addAll(entries);
            }
        }
        
        defaultEntries.addAll(getCimSubProdAttrMapMbMapping().get("CASHACC"));
        return defaultEntries.stream()
                .filter(distinctByKey(f -> f.getCustId() + f.getSubProduct() + f.getType()))
                .collect(Collectors.toList());
        
    }
    
    private List<CorporateFunction> getCorporateFunctions(List<Map<String, String>> rs) throws CustomException {
        
 
        List<FunctionId> functionIds = new ArrayList<>();         
        for(Map<String, String> map : rs)
        {
            String funcId = map.get("FUNC_ID");
            int wfModel = Integer.parseInt(map.get("WF_MODEL"));
            addToFunctionIds(funcId, wfModel, functionIds);            
        }        

        List<CorporateFunction> corpFunctions = new ArrayList<>();
        
        corpFunctions.addAll(getDefaultCorporateFunctions());
        List<CorporateFunction> bene_functions = getPaymntSubProdCodes().get("BENE");
        corpFunctions.addAll(addCorporateFunctions( bene_functions, "BENE", ""));  
        
        for (FunctionId functionId : functionIds) {
                                    
            List<String> paymntsubprodcodes = getPaymntSubProdCodes(functionId.getFuncId());
            for(String subprodcode : paymntsubprodcodes)
            {                                
                List<CorporateFunction> corp_functions = getPaymntSubProdCodes().get(subprodcode);
                if(corp_functions != null)
                    corpFunctions.addAll(addCorporateFunctions(corp_functions, "N", "N"));
            }                    

        }

        return corpFunctions.stream()
                .filter(distinctByKey(f -> f.getOdGcif()+f.getOdFunctionCode()+f.getOdProductCode()+f.getOdSubprodCode()))
                .collect(Collectors.toList());

    }

    private List<String> getPaymntSubProdCodes(String funcId)
    {
        List<String> subprodcodes = new ArrayList<>();
        
        if(ftcfg.a2atp.contains(funcId))
            subprodcodes.add("A2ATP");
        
        if(ftcfg.a2aself.contains(funcId))
            subprodcodes.add("A2ASELF");
        
        if(ftcfg.payroll.contains(funcId))
            subprodcodes.add("PAYROLL");
        
        if(ftcfg.dft_outward.contains(funcId)) {
            subprodcodes.add("DFT");
            subprodcodes.add("DFT-OUTWARD");
        }
        
        if(ftcfg.dft_wired.contains(funcId)) {            
            subprodcodes.add("DFT");
            subprodcodes.add("DFT-WIRED");            
        }
        
        if(ftcfg.cbft.contains(funcId))
            subprodcodes.add("CBFT");
        
        if(ftcfg.bene.contains(funcId))
            subprodcodes.add("BENE");
        
        if(ftcfg.billpay.contains(funcId))
            subprodcodes.add("BILLPAY");
        
        return subprodcodes;
    }    

    private void addToFunctionIds(String funcId, int wfModel, List<FunctionId> functionIds) {
        boolean exists = false;
        for (FunctionId functionId : functionIds) {
            if (functionId.getFuncId().equals(funcId)) {
                functionId.getWfModels().add(wfModel);
                exists = true;
                break;
            }
        }
        if (!exists) {
            FunctionId newEntry = FunctionId.builder()
                    .funcId(funcId)
                    .wfModels(new ArrayList<>())
                    .build();
            newEntry.getWfModels().add(wfModel);
            functionIds.add(newEntry);
        }
    }

    private List<UserFunction> getUserFunctions(List<Map<String, String>> rs, Input input) throws CustomException {
        List<UserFunction> userFunctions = new ArrayList<>();
        
        for(Map<String, String> map : rs)
        {            
            String login_id = map.get("OD_USER_NO");
            login_id = resultSetParser.replaceSpecialCharacters(login_id, dmService.getSpecialCharacters(), false);
            String default_domain = map.get("OD_GCIF");
            try {
                
                String org_id = get_org_id_mapping(cfg.inputPath).getOrDefault(input.getCorpCd(), new String[]{"", input.getCorpCd()})[0];
                
                BobUser bob_user = GenerateServiceImpl.bob_users.get(login_id + "-" + org_id);
                String domain_id = null;
                
                if(bob_user != null)
                    domain_id = bob_user.getDomain_id();
                
                if(domain_id != null)
                    default_domain = domain_id;
                
            } catch (GenerateException e) {
                throw new CustomException(e.getMessage(), e);
            }    

            UserFunction userFunction = UserFunction.builder()
                            .od_gcif(default_domain)
                            .od_user_no(map.get("OD_USER_NO"))
                            .od_subprod_code(map.get("OD_SUBPROD_CODE"))
                            .od_acc_no(map.get("OD_ACC_NO"))
                            .role(Integer.parseInt(map.get("ROLE")))
                            .rs(map)
                            .build();
            
            //**user functions _new
            List<UserFunction> usrFunctions = new ArrayList<>();
            switch(userFunction.getRole()) {
                case 0:  //maker
                    usrFunctions = getUserFunctionsDefaults("MAKER");
                    break;
                case 1: //verifier
                    usrFunctions = getUserFunctionsDefaults("VERIFIER");
                    break;
                case 2: //authorizer
                    usrFunctions = getUserFunctionsDefaults("AUTHORIZER");
                    break;
                case 3: //releaser
                    usrFunctions = getUserFunctionsDefaults("RELEASER");
                    break;
                case 4: //viewer
                    usrFunctions = getUserFunctionsDefaults("VIEWER");
                    break;
                case 6: //admin-maker
                case 7:
                    usrFunctions = getUserFunctionsDefaults("ADMIN MAKER");
                    break;
                case 8: //admin-checker
                    usrFunctions = getUserFunctionsDefaults("ADMIN CHECKER");
                    break;
            }
            
            for(UserFunction usrFunction : usrFunctions) {
                
                if(usrFunction.getOd_product_code().equalsIgnoreCase("PAYMNT")) {
                    
                    for(String paymntSubProdCode : getPaymntSubProdCodes(userFunction.getOd_subprod_code())) {
                        
                        if(paymntSubProdCode.equalsIgnoreCase(usrFunction.getOd_subprod_code())) {
                            
                            userFunctions.add(UserFunction.builder()
                                    .od_gcif(default_domain)
                                    .od_user_no(userFunction.getOd_user_no())
                                    .od_function_code(usrFunction.getOd_function_code())
                                    .od_subprod_code(paymntSubProdCode)
                                    .od_product_code(usrFunction.getOd_product_code())
                                    .od_acc_no(userFunction.getOd_acc_no())
                                    .criteria_type("ACC")
                                    .unit_id("ALL")
                                    .rs(map)
                                    .build()
                            );
                        }
                        
                    }
                    
                } else {
                    
                    userFunctions.add(UserFunction.builder()
                            .od_gcif(default_domain)
                            .od_user_no(userFunction.getOd_user_no())
                            .od_function_code(usrFunction.getOd_function_code())
                            .od_subprod_code(usrFunction.getOd_subprod_code())
                            .od_product_code(usrFunction.getOd_product_code())
                            .od_acc_no(userFunction.getOd_acc_no())
                            .criteria_type(usrFunction.getOd_product_code().equalsIgnoreCase("ADMIN") ? "ADMIN" : "ACC")
                            .unit_id("ALL")
                            .rs(map)
                            .build()
                    );
                    
                }
                
            }
            
        }                
                
        return userFunctions.stream().filter(distinctByKey(u -> u.getOd_gcif()+u.getOd_user_no()+u.getOd_function_code()+u.getOd_product_code()+u.getOd_subprod_code()+u.getOd_acc_no())).collect(Collectors.toList());
    }

    private static List <String[]> defaultFunctions;    
    private static List <String[]> getDefaultFunctions() throws CustomException {
        
        if(defaultFunctions == null) {
            
            defaultFunctions = new ArrayList<>();
            File file = new File("config/functions.csv");
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.readLine();
                String defaultFunction;
                while ((defaultFunction = br.readLine()) != null) {
                    
                    String[] details = defaultFunction.split(",");                                        
                    defaultFunctions.add(details);
                    
                }            
            } catch (IOException e) {
                log.info("Mapping file not found!");
                throw new CustomException(e.getMessage(), e);
            }
            
        }
        
        return defaultFunctions;
        
    }
    
    private static Map<String, List<UserFunction>> userFunctionsDefaults;
    private List<UserFunction> getUserFunctionsDefaults(String role) throws CustomException {
        
        if(userFunctionsDefaults == null) {
            
            userFunctionsDefaults = new HashMap<>();
            for(String[] function : getDefaultFunctions()) {
                List<UserFunction> userFunction = userFunctionsDefaults.get(function[0]);
                if(userFunction == null) {
                    userFunction = new ArrayList<>();
                }
                
                userFunction.add(UserFunction.builder()
                        .od_product_code(function[1])
                        .od_subprod_code(function[2])
                        .od_function_code(function[3])
                        .build()
                );
                
                userFunctionsDefaults.put(function[0], userFunction);
                
            }
            
        }
        
        return userFunctionsDefaults.get(role);
        
    }
    
    private static Map<String, List<UsergpFunction>> usergpFunctionsDefaults;    
    private List<UsergpFunction> getUsergpFunctionsDefaults(String usergroup) throws CustomException{
                
        if(usergpFunctionsDefaults == null) {
            
            usergpFunctionsDefaults = new HashMap<>();
            for(String[] function : getDefaultFunctions()){
                
                List<UsergpFunction> usrgpFunctions = usergpFunctionsDefaults.get(function[0]);                
                if(usrgpFunctions == null)
                {
                    usrgpFunctions = new ArrayList<>();                        
                }

                usrgpFunctions.add(UsergpFunction.builder()                           
                        .odProductCode(function[1])
                        .odSubprodCode(function[2])
                        .odFunctionCode(function[3])
                        .build()
                );

                usergpFunctionsDefaults.put(function[0], usrgpFunctions);
                
            }            
                        
        }
        
        return usergpFunctionsDefaults.get(usergroup);
        
    }
    
    private static Map<String, List<RulesAccMap>> rulesAccMapDefaults;
    private List<RulesAccMap> getRulesAccMapDefaults(String role) throws CustomException {
        
        if(rulesAccMapDefaults == null) {
            
            rulesAccMapDefaults = new HashMap<>();
            for(String[] function : getDefaultFunctions()){
                
                List<RulesAccMap> rulesAccMap = rulesAccMapDefaults.get(function[0]);
                if(rulesAccMap == null) 
                {
                    rulesAccMap = new ArrayList<>();
                }
                
                if(!function[1].equalsIgnoreCase("CORESVS")) {
                    rulesAccMap.add(RulesAccMap.builder()
                            .odProductCode(function[1])
                            .odSubproductCode(function[2])
                            .odFunctionId(function[3])
                            .build()
                    );
                }
                
                rulesAccMapDefaults.put(function[0], rulesAccMap);
                        
                
            }
            
        }
        
        return rulesAccMapDefaults.get(role);
        
    }
    
    private List<UsergpFunction> getUsergpFunctions(Input input, List<Map<String, String>> rs, Connection cibut_connection) throws CustomException {
        List<UsergpFunction> usergpFunctions = new ArrayList<>();
        List<UsergpFunction> container = new ArrayList<>();
        for(Map<String, String> map : rs) {
            container.add(
                    UsergpFunction.builder()
                            .odUsergroupCode(map.get("OD_USERGROUP_CODE"))
                            .odSubprodCode(map.get("OD_SUBPROD_CODE"))
                            .odAccNo(map.get("OD_ACC_NO"))
                            .role(Integer.parseInt(map.get("ROLE")))
                            .build()
            );
        }        

        // convert bob cg id to cms cg id
        for (UsergpFunction ugf : container) {
            
            String query = "SELECT CMS_CG_ID FROM CORP_GROUP_ID_CONVERT WHERE BOB_CG_ID = '" + ugf.getOdUsergroupCode() + "'";            
            try (                    
                    PreparedStatement stmt = cibut_connection.prepareStatement(query)
            ) {
                ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    ugf.setOdUsergroupCode(result.getString("CMS_CG_ID"));
                }
                
                result.close();
                stmt.close();                

            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new CustomException(e.getMessage(), e);
            }
            
//            usergpFunctions.addAll(getUsergpFunctionsDefaults());
            
            List<UsergpFunction> usrgpFunctions = new ArrayList<>();
            switch(ugf.getRole()){
                case 0:  //maker
                    usrgpFunctions = getUsergpFunctionsDefaults("MAKER");
                    break;
                case 1: //verifier
                    usrgpFunctions = getUsergpFunctionsDefaults("VERIFIER");
                    break;
                case 2: //authorizer
                    usrgpFunctions = getUsergpFunctionsDefaults("AUTHORIZER");
                    break;
                case 3: //releaser
                    usrgpFunctions = getUsergpFunctionsDefaults("RELEASER");
                    break;
                case 4: //viewer
                    usrgpFunctions = getUsergpFunctionsDefaults("VIEWER");
                    break;
                case 6:
                case 7:
                    usrgpFunctions = getUsergpFunctionsDefaults("ADMIN MAKER");
                    break;
                case 8:
                    usrgpFunctions = getUsergpFunctionsDefaults("ADMIN CHECKER");
                    break;
            }
            
            for(UsergpFunction usrgpFunction : usrgpFunctions)
            {
                if(usrgpFunction.getOdProductCode().equalsIgnoreCase("PAYMNT"))
                {
                    for(String paymntSubProdCode : getPaymntSubProdCodes(ugf.getOdSubprodCode()))
                    {
                        if(paymntSubProdCode.equalsIgnoreCase(usrgpFunction.getOdSubprodCode()))
                        {
                            usergpFunctions.add(UsergpFunction.builder()
                                            .odUsergroupCode(ugf.getOdUsergroupCode())
                                            .odFunctionCode(usrgpFunction.getOdFunctionCode())
                                            .odProductCode(usrgpFunction.getOdProductCode())
                                            .odSubprodCode(paymntSubProdCode)
                                            .odAccNo(ugf.getOdAccNo())
                                            .criteriaType("ACC")
                                            .unitId("ALL")
                                            .build());
                        }
                    } 
                } else {
                            
                    String product_code = usrgpFunction.getOdProductCode();
                    String sub_prod_code = usrgpFunction.getOdSubprodCode();
                    String function_code = usrgpFunction.getOdFunctionCode();
                    String od_acct_no = ugf.getOdAccNo();
                    String criteriaType = "ACC";
                    if(product_code.equalsIgnoreCase("ADMIN"))
                        criteriaType = "ADMIN";
                    if(product_code.equalsIgnoreCase("CORESVS") && sub_prod_code.equalsIgnoreCase("CORESVS") && function_code.equalsIgnoreCase("VSBLTY")) {
                        criteriaType = "CIF";
                        od_acct_no = input.getCif();
                    }
                        
                    usergpFunctions.add(UsergpFunction.builder()
                                        .odUsergroupCode(ugf.getOdUsergroupCode())
                                        .odFunctionCode(usrgpFunction.getOdFunctionCode())
                                        .odProductCode(usrgpFunction.getOdProductCode())
                                        .odSubprodCode(usrgpFunction.getOdSubprodCode())
                                        .odAccNo(od_acct_no)
                                        .criteriaType(criteriaType)
                                        .unitId("ALL")
                                        .build());

                }
            }                                    
        }
        return usergpFunctions.stream().filter(distinctByKey(f -> f.getOdUsergroupCode()+f.getOdFunctionCode()+f.getOdProductCode()+f.getOdSubprodCode()+f.getOdAccNo()))
                .collect(Collectors.toList());
    }

    private List<Usergroup> getUsergroups(List<Map<String, String>> rs, Connection cibut_connection) throws CustomException {
        List<Usergroup> usergroups = new ArrayList<>();
        try {
            for(Map<String, String> map : rs) {
                usergroups.add(
                        Usergroup.builder()
                                .odUsergroupCode(map.get("OD_USERGROUP_CODE"))
                                .odGcif(map.get("OD_GCIF"))
                                .odUsergroupName(map.get("OD_USERGROUP_NAME"))
                                .odUsergroupDesc(map.get("OD_USERGROUP_DESC"))
                                .odMakerId(map.get("OD_MAKER_ID"))
                                .odMakerDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(map.get("OD_MAKER_DATE")))
                                .odAuthId(map.get("OD_AUTH_ID"))
                                .odAuthDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(map.get("OD_AUTH_DATE")))
                                .odStatus(map.get("OD_STATUS"))
                                .odMakerName(map.get("OD_MAKER_NAME"))
                                .odAuthName(map.get("OD_AUTH_NAME"))
                                .odUsergroupType(map.get("OD_USERGROUP_TYPE"))
                                .build()
                );
            }
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            throw new CustomException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException(e.getMessage(), e);
        }

        // convert bob cg id to cms cg id
        for (Usergroup ug : usergroups) {
            String query = "SELECT CMS_CG_ID FROM CORP_GROUP_ID_CONVERT WHERE BOB_CG_ID = '" + ug.getOdUsergroupCode() + "'";
            try (                   
                   PreparedStatement stmt = cibut_connection.prepareStatement(query)
            ) {
                ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    ug.setOdUsergroupCode(result.getString("CMS_CG_ID"));
                }
                
                result.close();
                stmt.close();                

            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new CustomException(e.getMessage(), e);
            }
        }

        return usergroups.stream()
                .filter(distinctByKey(Usergroup::getOdUsergroupCode))
                .collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private List<UsergpLimit> getUsergpLimits(List<Map<String, String>> rs, Connection cibut_connection) throws CustomException {
        List<UsergpLimit> usergpLimits = new ArrayList<>();
        for(Map<String, String> map : rs) {
            String subprod = map.get("OD_SUBPROD_CODE");            
            usergpLimits.add(
                    UsergpLimit.builder()
                            .odGcif(map.get("OD_GCIF"))
                            .odUsergroupCode(map.get("OD_USERGROUP_CODE"))
                            .unitId("ALL")
                            .unitCcy(map.get("UNIT_CCY"))
                            .odDailyMaxAmt(new BigDecimal(map.get("OD_DAILY_MAX_AMT")))
                            .odDailyMaxNoTrans(map.get("OD_DAILY_MAX_NO_TRANS"))
                            .odDailyMaxAmtUpl(new BigDecimal(map.get("OD_DAILY_MAX_AMT_UPL")))
                            .odDailyMaxNoTransUpl(map.get("OD_DAILY_MAX_NO_TRANS_UPL"))
                            .odDailyMaxAuthAmt(new BigDecimal(map.get("OD_DAILY_MAX_AUTH_AMT")))
                            .odSelfFlag(map.get("OD_SELF_FLAG"))
                            .odSeltAuthAmt(new BigDecimal(map.get("OD_SELF_AUTH_AMT")))
                            .odApprovalFlag(map.get("OD_APPROVAL_FLAG"))
                            .odAmtMaskingFlag(map.get("OD_AMT_MASKING_FLAG"))
                            .dayConsolidatedTxnNo(map.get("DAY_CONSOLIDATED_TXN_NO"))
                            .dayConsolidatedTxnAmt(new BigDecimal(map.get("DAY_CONSOLIDATED_TXN_AMT")))
                            .dayConsolMaxApprovalAmt(new BigDecimal(map.get("DAY_CONSOL_MAX_APPROVAL_AMT")))
                            .dayMaxBulkTxnApprovalAmt(new BigDecimal(map.get("DAY_MAX_BULK_TXN_APPROVAL_AMT")))
                            .maxBulkTxnApprovalAmt(new BigDecimal(map.get("MAX_BULK_TXN_APPROVAL_AMT")))
                            .txnMaxApprLimitFlag(map.get("TXN_MAX_APPR_LIMIT_FLAG"))
                            .txnMaxApprovalAmt(new BigDecimal(map.get("TXN_MAX_APPROVAL_AMT")))
                            .dayMaxBulkApprLimitFlag(map.get("DAY_MAX_BULK_APPR_LIMIT_FLAG"))
                            .txnMaxBulkApprLimitFlag(map.get("TXN_MAX_BULK_APPR_LIMIT_FLAG"))
//                            .odProductCode("PAYMNT")
//                            .odSubprodCode(paymntSubprodCode != null ? paymntSubprodCode : subprod)
//                            .odTransMaxAmt(new BigDecimal(map.get("OD_TRANS_MAX_AMT")))
                            .build()
            );
        }

        for (UsergpLimit ugl : usergpLimits) {
            String query = "SELECT CMS_CG_ID FROM CORP_GROUP_ID_CONVERT WHERE BOB_CG_ID = '" + ugl.getOdUsergroupCode() + "'";
            try (                    
                    PreparedStatement stmt = cibut_connection.prepareStatement(query)
            ) {
                
                ResultSet result = stmt.executeQuery();                
                while (result.next()) {
                    ugl.setOdUsergroupCode(result.getString("CMS_CG_ID"));                    
                }
                
                result.close();
                stmt.close();                

            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new CustomException(e.getMessage(), e);
            }
        }

        return usergpLimits;
    }

    private List<Role> getRoles(List<Map<String, String>> rs, Input input) {
        List<Role> roles = new ArrayList<>();
        
        String corpCd = input.getCorpCd();
        rs.forEach((map) -> {
            roles.add(
                    Role.builder()
                            .odGcif(corpCd)
                            .odLevel(map.get("OD_LEVEL"))
                            .odRoleDesc(map.get("OD_ROLES").replace("-", " ").replace("ANY ", ""))
                            .build()
            );
        });
        
        return roles;
    }
    
    private Object[] parseRules(List<RuleContainer> rule_containers, int rule_id, Input input, Connection cib_connection) throws CustomException {
        
        List<Rule> rule_list = new ArrayList<>();
        List<RulesDef> rules_def_list = new ArrayList<>();
        List<ParsedRule> parsed_rule_list = new ArrayList<>();
        List<RulesAccMap> rulesAccMaps = new ArrayList<>();
                
        Map<String, List<RuleContainer>> matrix_map = new HashMap<>();

        int parsed_rule_id = rule_id;
        
        for(RuleContainer rule : rule_containers){
            
            rule.setRuleId(rule_id);
            
            String rule_name = rule.getDscp();
            String matrix_id = rule.getMatId();            
            List<RuleContainer> matrix_rules = matrix_map.get(matrix_id);
            if(matrix_rules == null) {                               
                matrix_rules = new ArrayList<>();                
                rule_list.add(Rule.builder()
                    .odGcif(input.getCorpCd())
                    .odRuleDesc(rule_name)                    
                    .odRuleId(rule_id++)
                    .odRuleName(rule_name)
                    .rule_matrix_id(matrix_id)
                    .build());
                
            }
                                    
            matrix_rules.add(rule);
            matrix_map.put(matrix_id, matrix_rules);                        
        }
        
        Map<String, List<RuleAcctContainer>> rac_map = new HashMap<>();
        List<RuleAcctContainer> ruleAcctContainers = getRuleAcctContainers(input, cib_connection);
        for (RuleAcctContainer rac : ruleAcctContainers) {
            
            List<RuleAcctContainer> rac_list = rac_map.get(rac.getMatrixId());
            if(rac_list == null) {
                rac_list = new ArrayList<>();                
            }
            
            rac_list.add(rac);
            rac_map.put(rac.getMatrixId(), rac_list);
        }
        
        for(Map.Entry<String, List<RuleAcctContainer>> rac_list : rac_map.entrySet()){
            for(RuleAcctContainer rac : rac_list.getValue()) {
                
                List<RuleContainer> ruleConList = matrix_map.get(rac.getMatrixId());
                
                if(ruleConList != null && !ruleConList.isEmpty()) {
                    rule_id = ruleConList.get(0).getRuleId();                

                    List<RulesAccMap> rulesAccMapFunctions = new ArrayList<>();
                    switch(rac.getRole()){
                        case 0:  //maker
                            rulesAccMapFunctions = getRulesAccMapDefaults("MAKER");
                            break;
                        case 1: //verifier
                            rulesAccMapFunctions = getRulesAccMapDefaults("VERIFIER");
                            break;
                        case 2: //authorizer
                            rulesAccMapFunctions = getRulesAccMapDefaults("AUTHORIZER");
                            break;
                        case 3: //releaser
                            rulesAccMapFunctions = getRulesAccMapDefaults("RELEASER");
                            break;
                        case 4: //viewer
                            rulesAccMapFunctions = getRulesAccMapDefaults("VIEWER");
                            break;
                        case 6:
                        case 7:
                            rulesAccMapFunctions = getRulesAccMapDefaults("ADMIN MAKER");
                            break;
                        case 8:
                            rulesAccMapFunctions = getRulesAccMapDefaults("ADMIN CHECKER");
                            break;
                    }

                    for(RulesAccMap rulesAccMap : rulesAccMapFunctions)
                    {                                                
                        if(rulesAccMap.getOdProductCode().equalsIgnoreCase("PAYMNT"))
                        {
                            for(String paymntSubProdCode : getPaymntSubProdCodes(rac.getFuncId()))
                            {
                                if(paymntSubProdCode.equalsIgnoreCase(rulesAccMap.getOdSubproductCode()))
                                {
                                    String odFunctionId, odProductCode, odSubproductCode, odOrgAccNo, criteriaType;


                                    odFunctionId = rulesAccMap.getOdFunctionId();
                                    odProductCode = rulesAccMap.getOdProductCode();
                                    odSubproductCode = paymntSubProdCode;
                                    odOrgAccNo = rac.getAcctNo();
                                    criteriaType = "ACC";

                                    rulesAccMaps.add(RulesAccMap.builder()
                                            .odRuleId(rule_id)                                                        
                                            .odFunctionId(odFunctionId)
                                            .odProductCode(odProductCode)
                                            .odSubproductCode(odSubproductCode)
                                            .odGcif(input.getCorpCd())
                                            .odOrgAccNo(odOrgAccNo)
                                            .criteriaType(criteriaType)
                                            .currency(rac.getCurrencyCd())
                                            .unitId("ALL")
                                            .build());
                                }
                            } 
                        } else {

                            String odFunctionId, odProductCode, odSubproductCode, odOrgAccNo, criteriaType;

                            odFunctionId = rulesAccMap.getOdFunctionId();
                            odProductCode = rulesAccMap.getOdProductCode();
                            odSubproductCode = rulesAccMap.getOdSubproductCode();
                            odOrgAccNo = rac.getAcctNo();
                            criteriaType = rulesAccMap.getOdProductCode().equalsIgnoreCase("ADMIN") ? "ADMIN" : "ACC";                        

                            rulesAccMaps.add(RulesAccMap.builder()
                                    .odRuleId(rule_id)
                                    .odFunctionId(odFunctionId)
                                    .odProductCode(odProductCode)
                                    .odSubproductCode(odSubproductCode)
                                    .odGcif(input.getCorpCd())
                                    .odOrgAccNo(odOrgAccNo)
                                    .criteriaType(criteriaType)
                                    .currency(rac.getCurrencyCd())
                                    .unitId("ALL")
                                    .build());

                        }

                    }

                }
            }
        }
        
        Map<String, List<RuleContainer>> matrix_rules_map = new HashMap<>();
        for(Map.Entry<String, List<RuleContainer>> matrix : matrix_map.entrySet()){                        
                                              
            for(RuleContainer rule_container : matrix.getValue()) {
                
                String matrix_rule_id = matrix.getKey() + "|" + rule_container.getMatRuleId();
                List<RuleContainer> matrix_rules = matrix_rules_map.get(matrix_rule_id);
                if(matrix_rules == null) {
                    matrix_rules = new ArrayList<>();
                }
                
                matrix_rules.add(rule_container);
                matrix_rules_map.put(matrix_rule_id, matrix_rules);
            }
        }
        
        for(Map.Entry<String, List<RuleContainer>> matrix_rules : matrix_rules_map.entrySet()){
                                    
            List<RuleContainer> rule_container_list = matrix_rules.getValue();                                             
            
            Map<Integer, List<RuleContainer>> role_rule_map=new TreeMap<>();
            for(RuleContainer rule_container : rule_container_list) {
                                
                List<RuleContainer> role_rule_list = role_rule_map.get(rule_container.getRole());
                if(role_rule_list == null)
                    role_rule_list = new ArrayList<>();
                
                role_rule_list.add(rule_container);
                role_rule_map.put(rule_container.getRole(), role_rule_list);
                
            }
                        
            int combination_count = 1;
            List<String[]> rule_info_list = new ArrayList<>();
            for(Map.Entry<Integer, List<RuleContainer>> role_rule : role_rule_map.entrySet()) {
                
                combination_count *= role_rule.getValue().size();                
            }                        
            
            String[] rule_info_arr = new String[combination_count];
            BigDecimal upper_limit = null;
            BigDecimal lower_limit = null;
            int rule_def_id = 0;
            
            List<List<String>> role_rules = new ArrayList<>();
            for(Map.Entry<Integer, List<RuleContainer>> role_rule : role_rule_map.entrySet()) {
                
                List<String> rules = new ArrayList<>();
                for(RuleContainer rule_container : role_rule.getValue()) {
                    
                    rules.add(rule_container.getRole() + ":" + rule_container.getRules());
                    
                }
                
                role_rules.add(rules);
                
            }                                    
            
            Map<Integer, List<String>> rules_map = new HashMap<>();
            List<String> rules_list = new ArrayList<>();
            
            for(Map.Entry<Integer, List<RuleContainer>> role_rule : role_rule_map.entrySet()) {
                List<RuleContainer> role_rule_list = role_rule.getValue();                                                
                
                for(RuleContainer rule_container : role_rule_list) {                    
                                       
                    int role = rule_container.getRole();
                    
                                        
                    String[] parsed_rules = rule_container.getRules().split(" & ");
                    String approver_details = "";                    
                    for(String parsed_rule : parsed_rules) {
                        
                        String[] parsed_rule_details = parsed_rule.split("_");
                        String approver_count = parsed_rule_details[0];
                        String approver_class = parsed_rule_details[1];
                        
                        String od_level = null;                    
                        if(role < 0) {
                            od_level = "NALL";
                        } else {
//                            od_level = "A" + (10 + role) + approver_class;
                        	try {
	                        	//get roles
	                        	PreparedStatement ps;
	                            ResultSet rs;
	                            List<Map<String, String>> rsmaplist = new ArrayList<>();
	                            String selectQuery = queriesService.getGnrtSelectQuery(ROLES);
	                            ps = cib_connection.prepareStatement(selectQuery, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
	                            int column_count = rs.getMetaData().getColumnCount();
	                            while(rs.next()){                                
	                            
	                                Map<String, String> rsmap = new HashMap<>();
	                                for(int y = 1; y <= column_count; y++)
	                                {                                 
	                                    String column_name = rs.getMetaData().getColumnName(y);
	                                    String value = rs.getString(column_name);
	
	                                    if(value != null)                       
	                                        value = value.replaceAll("\\|", "");
	
	
	                                    rsmap.put(column_name, value);
	
	
	
	                                }
	                                rsmaplist.add(rsmap);                
	                            }
	                            
	                            rs.close();
	                            ps.close();
	                            
	                            List<Role> roles = getRoles(rsmaplist, input);
	                            
	                            String roleDesc;            
	                            switch (role) {
	                                case 0:
	                                    roleDesc = "MAKER";
	                                    break;
	                                case 1:
	                                    roleDesc = approver_class + " VERIFIER";
	                                    break;
	                                case 2:
	                                    roleDesc = approver_class + " AUTHORIZER";
	                                    break;
	                                case 3:
	                                    roleDesc = approver_class + " RELEASER";
	                                    break;
	                                case 4:
	                                    roleDesc = "VIEWER";
	                                    break;              
	                                default:
	                                    roleDesc = "CORPADMIN";
	                            }
	                            
	                            if (roleDesc.equals("CORPADMIN"))
	                            	od_level = "A99";
	                            else {
	                                for (Role approverRole : roles) {
	                                    if (roleDesc.equals(approverRole.getOdRoleDesc())){
	                                    	od_level = approverRole.getOdLevel();
	                                    }                                
	                                }
	                            }
	                        } catch (SQLException | IllegalArgumentException e) {
	                            log.error(e.getMessage(), e);
//	                            throw new GenerateException(e.getMessage(), e);
	                        } 
                        	
                            
                        }
                        
                        approver_details += " & " + approver_count + "_" + od_level;                                               
                        
                    }
                                                          
                    approver_details = approver_details.replaceFirst(" & ", "");
                    
                    if(approver_details.contains(" & ")) {
                        approver_details = "( " + approver_details + " )";
                    }
                    
                    rules_list = rules_map.get(role);
                    if(rules_list == null)
                        rules_list = new ArrayList<>();
                    
                    rules_list.add(approver_details);
                    rules_map.put(role, rules_list);                    
                    
                }                                                                                   
            }
            
            List<List<String>> rule_info_lists = new ArrayList<>();
            for(Map.Entry<Integer, List<String>> rules : rules_map.entrySet()) {                
                rule_info_lists.add(rules.getValue());
            }
            
            List<List<String>> rule_infos = Generator.cartesianProduct(rule_info_lists).stream().collect(Collectors.toList());
            
            String rule_info_combined = "( ";
            int rule_info_counter = 1;
            for(List<String> rule_info : rule_infos) {                               
                                
                int counter = 1;
                for(String rule_info_details : rule_info) {
                                        
                    rule_info_combined += rule_info_details;
                    if(rule_info.size() > 1 && counter < rule_info.size())
                        rule_info_combined += " & ";
                    
                    counter++;
                        
                }                                
                
                if(rule_info_counter < rule_infos.size())
                    rule_info_combined += " ~ ";
                
                rule_info_counter++;
                
            }
            
            rule_info_combined += " )";            
            
            upper_limit = rule_container_list.get(0).getUpperLimit();
            lower_limit = rule_container_list.get(0).getLowerLimit();
            rule_def_id = matrix_map.get(rule_container_list.get(0).getMatId()).get(0).getRuleId();

            for(String rule_info : rule_info_combined.split(" ~ ")) {
                
                rules_def_list.add(RulesDef.builder()
                        .odMaxAmt(upper_limit)
                        .odMinAmt(lower_limit)
                        .odRuleId(rule_def_id)
                        .odRuleParseId(parsed_rule_id)
                        .ruleInfo(rule_info_combined.contains("NALL") ? "NA" : rule_info_combined)
                        .build());
                
                String[] parsed_rule_arr = rule_info.replace(" ", "").replace("(", "").replace(")", "").replace("&", "~").split("~");
                
                for(String parsed_rule : parsed_rule_arr) {
                    
                    String[] parsed_rule_details = parsed_rule.split("_");
                    String approver_count = parsed_rule_details[0];
                    String od_level = parsed_rule_details[1];                    
                        
                    parsed_rule_list.add(ParsedRule.builder()
                            .approvalFlow("S")
                            .odCount(Integer.parseInt(approver_count))
                            .odLevel(od_level)
                            .odRuleParseId(parsed_rule_id)
                            .build());
                }
                                
                parsed_rule_id++;
                
            }
                
        }
                                                       
        
        return new Object[]{rule_list, rules_def_list, parsed_rule_list, rulesAccMaps};
    }
    
    private Rules getRules(List<Map<String, String>> rs, Input input, int ruleId, int parsedRuleId, Connection cib_connection) throws CustomException {
        List<RuleContainer> ruleContainers = new ArrayList<>();
        
        for(Map<String, String> map : rs)
        {            
            RuleContainer rulCon = RuleContainer.builder()
                    .ruleId(ruleId)
                    .dscp(map.get("DSCP"))
                    .matId(map.get("MAT_ID"))
                    .matRuleId(map.get("MAT_RULE_ID"))
                    .lowerLimit(new BigDecimal(map.get("LOWER_LIMIT")))
                    .upperLimit(new BigDecimal(map.get("UPPER_LIMIT")))
                    .role(Integer.parseInt(map.get("ROLE") == null ? "-1" : map.get("ROLE")))
                    .rules(map.get("RULES"))
                    .build();
                   
                ruleContainers.add(rulCon);            
        }

        Object[] corp_rules = parseRules(ruleContainers, ruleId, input, cib_connection);
        List<Rule> rules = (List<Rule>) corp_rules[0];
        List<RulesDef> rulesDefs = (List<RulesDef>) corp_rules[1];
        List<ParsedRule> parsedRules = (List<ParsedRule>) corp_rules[2];
        List<RulesAccMap> rulesAccMaps = (List<RulesAccMap>) corp_rules[3];              
        
        return Rules.builder()
                .ruleList(rules)
                .rulesDefList(rulesDefs)
                .parsedRuleList(parsedRules)
                .rulesAccMapList(rulesAccMaps.stream().filter(distinctByKey(r -> r.getOdRuleId()+r.getOdFunctionId()+r.getOdProductCode()+r.getOdSubproductCode()+r.getOdOrgAccNo()+r.getOdGcif())).collect(Collectors.toList()))
                .build();
    }        

    private List<RuleAcctContainer> getRuleAcctContainers(Input input, Connection cib_connection) throws CustomException {
        
        List<RuleAcctContainer> ruleAcctContainers = new ArrayList<>();
        ResultSet rs = null;
        
        try (              
              PreparedStatement ps = cib_connection.prepareStatement(qcfg.gnrtSelectRulesAcct);
        ) {
            
            List<Input> checkedInputs = DmServiceImpl.checkedInputs;
            for(Input checkedInput : checkedInputs) {
                if(checkedInput.getCorpCd().equalsIgnoreCase(input.getCorpCd())){
                    
                    ps.setString(1, input.getCorpCd());
                    ps.setString(2, input.getCorpCd());
                    ps.setString(3, checkedInput.getCif());

                    rs = ps.executeQuery();

                    while (rs.next()) {
                        ruleAcctContainers.add(
                                RuleAcctContainer.builder()
                                        .matrixId(rs.getString("MATRIX_ID"))
                                        .tranMatrixId(rs.getString("TRAN_MATRIX_ID"))
                                        .funcId(rs.getString("FUNC_ID"))
                                        .currencyCd(rs.getString("CURRENCY_CD"))
                                        .acctNo(rs.getString("ACCT_NO"))
                                        .role(rs.getInt("ROLE"))
                                        .build()
                        );
                    }

                }
            }
            
            if(rs != null)
                rs.close();
            
            ps.close();
        
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new CustomException(e.getMessage(), e);
        }
        return ruleAcctContainers;
    }


    private String toStringDate(Date date) {
        return new SimpleDateFormat(fcfg.dateFormat).format(date);
    }

    @Override
    public List<String> insertDefaultValuesToDb(String insert_query, String tableName, Map<String, String> field_value_map, Input input, Connection cibut_connection, Connection cib_connection) throws CustomException {
        
        List<String> records = new ArrayList<>();
        
        try {
            
            String[] fields = insert_query.substring(insert_query.indexOf("(") + 1, insert_query.indexOf(")")).split(", ");   
            PreparedStatement ps = cibut_connection.prepareStatement(insert_query);
            String parsedRecord = "";
            int counter = 1;
            for(String field : fields)
            {                
                String value = field_value_map.get(field);                
                
                if(value != null) {
                    
                    if(value.equals("[CIF]"))
                    {                    
                        value = input.getCif();
                    }

                    if(value.equals("[CORP_CD]"))
                    {
                        value = input.getCorpCd();
                    }

                    if(value.startsWith("[SUBQUERY"))
                    {
                        int result_ctr = 0;
                        String[] valueArr = value.replaceAll("\\[|\\]", "").split("\\.");
                        String subquery = getGnrtSelectSubQuery(valueArr[1], valueArr[2]);                    
                        PreparedStatement subps = cib_connection.prepareStatement(subquery);
                        int ps_arg_ctr = 1;
                        for(int x = 3; x < valueArr.length; x++)
                        {
                            String arg_name = valueArr[x];
                            if(arg_name.equalsIgnoreCase("CORP_CODE"))
                                subps.setString(ps_arg_ctr, input.getCorpCd());
                            else if(arg_name.equalsIgnoreCase("CIF"))
                                subps.setString(ps_arg_ctr, input.getCif());
                            ps_arg_ctr++;
                        }
                        ResultSet subrs = subps.executeQuery();                    
                        while(subrs.next())
                        {                        
                            value = subrs.getString("VALUE");    
                            result_ctr++;
                        }
                        subrs.close();
                        subps.close();
                        
                        if(result_ctr == 0)
                        {
                            if(tableName.equalsIgnoreCase(CIM_CUST_BILLING_DETAILS_MB) && field.equalsIgnoreCase("OTHER_ACCOUNT"))
                                throw new CustomException("[" + tableName + "][" + input.getCif() + "/" + input.getCorpCd() + "]No accounts found.", new Exception());
                        }
                        
                    }
                    
                }
                                
                ps.setString(counter, value);
                parsedRecord += (value == null ? "" : value) + "|";
                
                counter++;
            }
                        
            ps.executeUpdate();
            ps.close();
          
            records.add(parsedRecord.substring(0, parsedRecord.lastIndexOf("|")));                        
            
        } catch (SQLIntegrityConstraintViolationException e) {
            
            log.error("[{}][" + input.getCif() + "/" + input.getCorpCd() + "]|Batch Insert Failed : {} records affected due to {}", tableName, e.getMessage().trim(), records.size());                                                                        
            
        } catch (SQLException e) {
            
            log.error(e.getMessage(), e);
//            throw new CustomException(e.getMessage(), e);
            
        } catch (CustomException e) {
            
            if (e.getMessage().endsWith("No accounts found.")) {
                log.error(e.getMessage());
            } else
                throw new CustomException(e.getMessage(), e);
            
        }
        
        return records;
    }
    
    private String getGnrtSelectSubQuery(String tableName, String field) {
        
        String subquery = null;
        switch(tableName) {
            case CIM_CUST_BILLING_DETAILS_MB:
                switch(field){
                    case "OTHER_ACCOUNT":
                        subquery = qcfg.gnrtSelectCimCustBillingDetailsMbOtherAccount;
                        break;
                }
                break;
        }
               
        return subquery;
    }

} 
