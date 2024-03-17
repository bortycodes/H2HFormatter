package com.bdo.cms.bob_data_migration_utility.service.queries;

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
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.ROLES;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.RULES;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bdo.cms.bob_data_migration_utility.config.BobDatabaseConfig;
import com.bdo.cms.bob_data_migration_utility.config.FieldsAndTablesConfig;
import com.bdo.cms.bob_data_migration_utility.config.QueriesConfig;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.QueriesServiceException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QueriesServiceImpl implements QueriesService {

    @Autowired
    QueriesConfig qcfg;

    @Autowired
    BobDatabaseConfig bobcfg;

    @Autowired
    FieldsAndTablesConfig ftcfg;


    @Override
    public String getGnrtSelectQuery(String tableName) {
        String query;
        switch (tableName) {
            case CIM_CUST_DEFN_MB:
                query = qcfg.gnrtSelectCimCustDefnMb;
                break;
            case CIM_CUST_ACCT_MB:
                query = qcfg.gnrtSelectCimCustAcctMb;
                break;
            case CIM_CUST_CONTACT_INFO_MB:
                query = qcfg.gnrtSelectCimCustContactInfoMb;
                break;
            case CIM_CUST_BILLING_DETAILS_MB:
                query = qcfg.gnrtSelectCimCustBillingDetailsMb;
                break;
            case CIM_PAYMENT_BKFT_DT_MB:
                query = qcfg.gnrtSelectCimPaymentBkftDtMb;
                break;
            case CIM_PAYMENT_CUST_PREF_MB:
                query = qcfg.gnrtSelectCimPaymentCustPrefMb;
                break;
            case CIM_PAYMENT_PARAMS_MB:
                query = qcfg.gnrtSelectCimPaymentParamsMb;
                break;
            case CIM_BUSINESS_PARAMS_MB:
                query = qcfg.gnrtSelectCimBusinessParamsMb;
                break;
            case CIM_DOMAIN_DEFN:
                query = qcfg.gnrtSelectCimDomainDefn;
                break;
            case CIM_CUSTOMER_LIMIT_MB:
                query = qcfg.gnrtSelectCimCustomerLimitMb;
                break;
            case OD_CORPORATE_LIMITS_MB:
                query = qcfg.gnrtSelectOdCorporateLimitsMb;
                break;
            case OD_USERGROUP_MB:
                query = qcfg.gnrtSelectOdUsergroupMb;
                break;
            case OD_CORPORATE_FUNCTION_MB:
                query = qcfg.gnrtSelectOdCorporateFunctionMb;
                break;
            case OD_USERS_MB:
                query = qcfg.gnrtSelectOdUsersMb;
                break;
            case OD_USER_LIMITS_MB:
                query = qcfg.gnrtSelectOdUserLimitsMb;
                break;
            case ORBIIBS_NICKNAME:
                query = qcfg.gnrtSelectOrbiibsNickname;
                break;
            case OD_USER_FUNCTION_MB:
                query = qcfg.gnrtSelectOdUserFunctionMb;
                break;
            case OD_USERGP_FUNCTION_MB:
                query = qcfg.gnrtSelectOdUsergpFunctionMb;
                break;
            case OD_USERGP_LIMITS_MB:
                query = qcfg.gnrtSelectOdUsergpLimitsMb;
                break;
            case ROLES:
                query = qcfg.gnrtSelectRoles;
                break;
            case RULES:
                query = qcfg.gnrtSelectRules;
                break;
            case BENEFICIARY_MAINTENANCE:
                query = qcfg.gnrtSelectBeneficiaryMaintenance;
                break;
            case CIM_SUBPROD_ATTR_MAP_MB:
                query = qcfg.gnrtSelectCimSubProdAttrMapMb;
                break;
            default:
                query = null;
        }
        return query;
    }

    @Override
    public String getMgrtSelectQuery(String tableName) {
        String query;
        switch (tableName) {
            case CIM_CUST_DEFN_MB:
                query = qcfg.mgrtSelectCimCustDefnMb;
                break;
            case CIM_CUST_ACCT_MB:
                query = qcfg.mgrtSelectCimCustAcctMb;
                break;
            case CIM_CUST_CONTACT_INFO_MB:
                query = qcfg.mgrtSelectCimCustContactInfoMb;
                break;
            case CIM_DOMAIN_DEFN:
                query = qcfg.mgrtSelectCimDomainDefn;
                break;
            case CIM_CUSTOMER_LIMIT_MB:
                query = qcfg.mgrtSelectCimCustomerLimitMb;
                break;
            case OD_CORPORATE_LIMITS_MB:
                query = qcfg.mgrtSelectOdCorporateLimitsMb;
                break;
            case OD_USERGROUP_MB:
                query = qcfg.mgrtSelectOdUsergroupMb;
                break;
            case OD_CORPORATE_FUNCTION_MB:
                query = qcfg.mgrtSelectOdCorporateFunctionMb;
                break;
            case OD_USERS_MB:
                query = qcfg.mgrtSelectOdUsersMb;
                break;
            case OD_USER_LIMITS_MB:
                query = qcfg.mgrtSelectOdUserLimitsMb;
                break;
            case ORBIIBS_NICKNAME:
                query = qcfg.mgrtSelectOrbiibsNickname;
                break;
            case OD_USER_FUNCTION_MB:
                query = qcfg.mgrtSelectOdUserFunctionMb;
                break;
            case OD_USERGP_FUNCTION_MB:
                query = qcfg.mgrtSelectOdUsergpFunctionMb;
                break;
            case OD_USERGP_LIMITS_MB:
                query = qcfg.mgrtSelectOdUsergpLimitsMb;
                break;
            case OD_ROLES_MB:
                query = qcfg.mgrtSelectOdRolesMb;
                break;
            case OD_RULES_MB:
                query = qcfg.mgrtSelectOdRulesMb;
                break;
            case OD_RULES_DEF_MB:
                query = qcfg.mgrtSelectOdRulesDefMb;
                break;
            case OD_PARSED_RULE_MB:
                query = qcfg.mgrtSelectOdParsedRuleMb;
                break;
            case OD_RULES_ACC_MAP_MB:
                query = qcfg.mgrtSelectOdRulesAccMapMb;
                break;
            case OD_USER_ROLES_MAP_MB:
                query = qcfg.mgrtSelectOdUserRolesMapMb;
                break;
            case BENEFICIARY_MAINTENANCE:
                query = qcfg.mgrtSelectBeneficiaryMaintenance;
                break;
            case CIM_CUST_BILLING_DETAILS_MB:
                query = qcfg.mgrtSelectCimCustBillingDetailsMb;
                break;
            case CIM_PAYMENT_BKFT_DT_MB:
                query = qcfg.mgrtSelectCimPaymentBkftDtMb;
                break;
            case CIM_PAYMENT_CUST_PREF_MB:
                query = qcfg.mgrtSelectCimPaymentCustPrefMb;
                break;
            case CIM_PAYMENT_PARAMS_MB:
                query = qcfg.mgrtSelectCimPaymentParamsMb;
                break;
            case CIM_BUSINESS_PARAMS_MB:
                query = qcfg.mgrtSelectCimBusinessParamsMb;
                break;
            case CIM_SUBPROD_ATTR_MAP_MB:
                query = qcfg.mgrtSelectCimSubProdAttrMapMb;
                break;
            default:
                query = null;
        }
        return query;
    }


    @Override
    public List<Input> removeDuplicates(List<Input> inputs, Connection cibut_connection) throws QueriesServiceException {
        log.info("[DUPLICATE CHECK] - Checking and removing duplicate inputs...");
        List<Input> retrievedInputs = new ArrayList<>();

        try (                
                PreparedStatement preparedStatement = cibut_connection.prepareStatement(qcfg.selectProcessed)
        ) {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                retrievedInputs.add(Input.builder()
                        .cif(rs.getString("CIF"))
                        .corpCd(rs.getString("CORP_CD"))
                        .build());
            }
            
            rs.close();
            preparedStatement.close();            
            
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }

        String cifs = "";
        List<Input> uniqueInputs = new ArrayList<>();
        for (Input i : inputs) {
            boolean found = false;
            for (Input j : retrievedInputs) {
                if (i.getCif().equals(j.getCif()) && i.getCorpCd().equals(j.getCorpCd())) {                    
                    if(!i.getOverride().equalsIgnoreCase("Y")) {
                        found = true;    
                        break;
                    } else
                    {
                        try (                                               
                        PreparedStatement ps = cibut_connection.prepareStatement("delete from processed_inputs where cif = ? and corp_cd = ?");
                        ){
                            ps.setString(1, j.getCif());
                            ps.setString(2, j.getCorpCd());
                            int result = ps.executeUpdate();
                            if(result > 0)
                                log.info("Overriding extraction and migration for {}-{}", j.getCif(), j.getCorpCd());
                            else {
                                found = true;
                                log.info("Unable to override {}/{}" + j.getCif(), j.getCorpCd());
                            }
                            
                            ps.close();                            
                            
                        } catch (SQLException ex) {
                            
                            log.error(ex.getMessage(), ex);
                            throw new QueriesServiceException(ex.getMessage(), ex);
                            
                        }
                    }
                }
            }
            if (!found) {
                uniqueInputs.add(i);
                cifs += "'" + i.getCif() + "', ";
            }
        }
        log.info("[DUPLICATE CHECK] - Checking and removing duplicates done.");
        
        //check if CIF is already in CMS
//        String select_cifs = "SELECT CUST_ID FROM cim_cust_defn_mb WHERE cust_id IN ("+ cifs.replaceAll(", $", "") +")";
//        List<String> cust_in_cms = new ArrayList<>();
//        
//        try {
//            
//            PreparedStatement ps = cms_connection.prepareStatement(select_cifs);
//            ResultSet rs = ps.executeQuery();
//            while(rs.next()) {
//                
//                cust_in_cms.add(rs.getString("CUST_ID"));
//                
//            }
//            
//            rs.close();
//            ps.close();
//            
//            if(!cust_in_cms.isEmpty()) {
//                
//                Iterator<Input> it = uniqueInputs.iterator();
//                while(it.hasNext()) {
//                    Input input = it.next();
//                    if(cust_in_cms.contains(input.getCif())) {
//                        it.remove();
//                        log.info("{}/{} - CIF already in CMS.", input.getCif(), input.getCorpCd());
//                    }
//                }
//                
//            }
//            
//        } catch (SQLException ex) {
//            
//            log.error(ex.getMessage(), ex);
//            throw new QueriesServiceException(ex.getMessage(), ex);
//            
//        }
        
        return uniqueInputs;
    }


    @Override
    public void saveProcessedInputs(List<Input> inputs) throws QueriesServiceException {
        if (!inputs.isEmpty()) {
            try (
                    Connection connection = bobcfg.cibutDb().getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(qcfg.gnrtSaveInputs)
            ) {
                for (Input input : inputs) {
                    preparedStatement.setString(1, input.getCif());
                    preparedStatement.setString(2, input.getCorpCd());

                    preparedStatement.execute();
                }
                
                preparedStatement.close();
                connection.close();
                
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new QueriesServiceException(e.getMessage(), e);
            }
            log.info("Processed Inputs saved.");
        } else {
            log.info("No new inputs processed.");
        }

    }

    @Override
    public List<Input> retrieveGenerated() throws QueriesServiceException {
        List<Input> retrievedInputs = new ArrayList<>();

        try (
                Connection connection = bobcfg.cibutDb().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(qcfg.mgrtRetrieve)
        ) {
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                retrievedInputs.add(Input.builder()
                        .cif(rs.getString("CIF"))
                        .corpCd(rs.getString("CORP_CD"))
                        .build());
            }
            
            rs.close();
            preparedStatement.close();
            connection.close();
            
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }

        return retrievedInputs;
    }

    @Override
    public void updateProcessedInputs(List<Input> inputs) throws QueriesServiceException {
        if (!inputs.isEmpty()) {
        	List<String> cifsToUpdate = new ArrayList<>();
            StringBuilder cifs = new StringBuilder();
            for (int i = 0, cifCount = 1; i < inputs.size(); i++, cifCount++) { //parse CIFs to 1k per query to comply to Oracle's IN clause max expression limit of 1K
            	if(cifCount <= 1000) {
	                Input input = inputs.get(i);
	                cifs.append(input.getCif()).append(cifCount == 1000 || i == inputs.size() - 1 ? "" : ",");
            	}
            	if(cifCount == 1000 || i == inputs.size() - 1) {
            		cifCount = 0;
            		cifsToUpdate.add(cifs.toString());
            		cifs.setLength(0);
            	}
            }
            int[] processedInputsUpdated = null;
            try (
	    		Connection connection = bobcfg.cibutDb().getConnection();
            	Statement stmt = connection.createStatement()
            ) {
            	String query = null;
            	for(String cif : cifsToUpdate) {
                	query = "UPDATE PROCESSED_INPUTS SET FLAG = 3 WHERE CIF IN (" + cif + ")";
                	stmt.addBatch(query);
                }
            	
                processedInputsUpdated = stmt.executeBatch();
                stmt.clearBatch();
                stmt.close();
                connection.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new QueriesServiceException(e.getMessage(), e);
            }
            log.info("{} Processed Inputs updated.", Arrays.stream(processedInputsUpdated).sum());
        } else {
            log.info("No new inputs migrated.");
        }

    }

    @Override
    public void deleteInvalidInput(Connection cibut_connection) throws QueriesServiceException {
        log.info("[DELETE INVALID INPUTS] - Deleting inputs with flag = 2...");
        try (                
                PreparedStatement ps = cibut_connection.prepareStatement(qcfg.delete)
        ) {
            ps.execute();
            ps.close();            
            
            truncateTempTables(cibut_connection);
            
            log.info("[DELETE INVALID INPUTS] - Deleted.");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }

    }

    @Override
    public void truncateTempTables(Connection cibut_connection) throws QueriesServiceException {
        log.info("[TRUNCATE] - Truncating temp tables...");
        List<String> tableNames = ftcfg.tableNames;

        String query = "TRUNCATE TABLE ";

        try (                
                Statement stmt = cibut_connection.createStatement()
        ) {
            for (String tableName : tableNames) {
                if (tableName.equals(ROLES)) {
                    stmt.addBatch(query + OD_ROLES_MB);
                    stmt.addBatch(query + OD_USER_ROLES_MAP_MB);

                } else if (tableName.equals(RULES)) {
                    stmt.addBatch(query + OD_RULES_MB);
                    stmt.addBatch(query + OD_RULES_DEF_MB);
                    stmt.addBatch(query + OD_PARSED_RULE_MB);
                    stmt.addBatch(query + OD_RULES_ACC_MAP_MB);

                } else {
                    stmt.addBatch(query + tableName);
                }
            }

            stmt.executeBatch();
            stmt.close();            
            log.info("[TRUNCATE] - Truncated.");

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }

    }
    

	@Override
	public void truncateCMSrawTables(Connection cms_connection) throws QueriesServiceException {
		log.info("[TRUNCATE] - Truncating CMS RAW tables...");
        List<String> cmsRawTables = ftcfg.cmsRawTables;

        String query = "TRUNCATE TABLE ";
        String endQuery = " DROP storage";

        try (                
                Statement stmt = cms_connection.createStatement()
        ) {
            for (String tableName : cmsRawTables) {
            	stmt.addBatch(query + tableName + endQuery);
            }

            stmt.executeBatch();
            stmt.close();            
            log.info("[TRUNCATE] - CMS Raw Tables Truncated.");

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }
	}

    @Override
    public boolean checkDuplicate(String tableName, Input input, Connection cibut_connection) throws QueriesServiceException {
        
        try {
            
            String query_cif = "select count(1) as row_count from table_name where CUST_ID = ?";
            String query_corp_cd = "select count(1) as row_count from table_name where OD_GCIF = ?";            
            
            PreparedStatement ps;
            
            
            switch(tableName){                
                case CIM_CUST_DEFN_MB:
                case CIM_CUST_CONTACT_INFO_MB:
                case CIM_CUST_ACCT_MB:
                case CIM_CUSTOMER_LIMIT_MB:  
                case CIM_CUST_BILLING_DETAILS_MB: 
                case CIM_PAYMENT_CUST_PREF_MB:
                    ps = cibut_connection.prepareStatement(query_cif.replace("table_name", tableName));                    
                    ps.setString(1, input.getCif());
                    break;
                case OD_CORPORATE_LIMITS_MB:
                case OD_USER_LIMITS_MB:
                    ps = cibut_connection.prepareStatement(query_corp_cd.replace("table_name", tableName));                                        
                    ps.setString(1, input.getCorpCd());
                    break;
                default:
                    return true;                    
            }
            
            
            ResultSet rs = ps.executeQuery();
            int row_count = 0;
            while(rs.next()){                
                row_count = rs.getInt("row_count");                                
            }
            
            rs.close();
            ps.close();            
            
            return (row_count > 0);
                        
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }
                
    }

    @Override
    public boolean checkPaymentsDuplicate(String tableName, Input input, String payment_product_field_name, String payment_product_name, Connection cibut_connection) throws QueriesServiceException {
        try {
            
            String query = "select count(1) as row_count from table_name where CUST_ID = ? and " + payment_product_field_name + " = ?";
                        
            PreparedStatement ps;
            
            
            switch(tableName){                
                case CIM_PAYMENT_BKFT_DT_MB:
                case CIM_PAYMENT_PARAMS_MB:
                case CIM_BUSINESS_PARAMS_MB:
                    ps = cibut_connection.prepareStatement(query.replace("table_name", tableName));                    
                    ps.setString(1, input.getCif());
                    ps.setString(2, payment_product_name);
                    break; 
                default:
                    return true;                    
            }
            
            
            ResultSet rs = ps.executeQuery();
            int row_count = 0;
            while(rs.next()){                
                row_count = rs.getInt("row_count");                                
            }
            
            rs.close();
            ps.close();            
            
            return (row_count > 0);
                        
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new QueriesServiceException(e.getMessage(), e);
        }
    }

    @Override
    public String getGnrtSelectSubQuery(String tableName, String field) {
        
        String subquery = null;
        switch(tableName){
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
