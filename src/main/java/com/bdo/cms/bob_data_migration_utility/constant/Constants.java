package com.bdo.cms.bob_data_migration_utility.constant;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class Constants {

    private Constants() {}

    public static Constants getInstance() {
        return ConstantsHolder.INSTANCE;
    }

    private static class ConstantsHolder {

        private static final Constants INSTANCE = new Constants();
    }

    public static final SimpleDateFormat FMT_YYMMDDHHMMSS = new SimpleDateFormat("yyMMddhhmmss");
    public static final String COMMA_SPACE = ", ";
    public static final String CORP_GROUP_ID_CONVERT = "CORP_GROUP_ID_CONVERT";

//    public static final List<String> MAKER_FUNCTIONS = Arrays.asList("AMNDSI", "LOCKTXN", "RCTXN", "UPLOAD", "create", "inquiry");
//    public static final List<String> VERIFIER_FUNCTIONS = Arrays.asList("TXNVER", "inquiry");
//    public static final List<String> AUTHORIZER_FUNCTIONS = Arrays.asList("AUTTXN", "CNCLSI", "CNTXN", "inquiry");
//    public static final List<String> RELEASER_FUNCTIONS = Arrays.asList("SNDTXN", "inquiry");
//    public static final List<String> VIEWER_FUNCTIONS = Arrays.asList("inquiry", "siinquiry");

    public static final String CIM_CUST_DEFN_MB = "CIM_CUST_DEFN_MB";
    public static final String CIM_CUST_ACCT_MB = "CIM_CUST_ACCT_MB";
    public static final String CIM_CUST_CONTACT_INFO_MB = "CIM_CUST_CONTACT_INFO_MB";
    public static final String CIM_CUST_BILLING_DETAILS_MB = "CIM_CUST_BILLING_DETAILS_MB";
    public static final String CIM_PAYMENT_BKFT_DT_MB = "CIM_PAYMENT_BKFT_DT_MB";
    public static final String CIM_PAYMENT_CUST_PREF_MB = "CIM_PAYMENT_CUST_PREF_MB";
    public static final String CIM_PAYMENT_PARAMS_MB = "CIM_PAYMENT_PARAMS_MB";
    public static final String CIM_BUSINESS_PARAMS_MB = "CIM_BUSINESS_PARAMS_MB";
    public static final String CIM_DOMAIN_DEFN = "CIM_DOMAIN_DEFN";
    public static final String CIM_CUSTOMER_LIMIT_MB = "CIM_CUSTOMER_LIMIT_MB";
    public static final String OD_CORPORATE_LIMITS_MB = "OD_CORPORATE_LIMITS_MB";
    public static final String OD_USERGROUP_MB = "OD_USERGROUP_MB";
    public static final String OD_CORPORATE_FUNCTION_MB = "OD_CORPORATE_FUNCTION_MB";
    public static final String OD_USERS_MB = "OD_USERS_MB";
    public static final String OD_USER_LIMITS_MB = "OD_USER_LIMITS_MB";
    public static final String ORBIIBS_NICKNAME = "ORBIIBS_NICKNAME";
    public static final String OD_USER_FUNCTION_MB = "OD_USER_FUNCTION_MB";
    public static final String OD_USERGP_FUNCTION_MB = "OD_USERGP_FUNCTION_MB";
    public static final String OD_USERGP_LIMITS_MB = "OD_USERGP_LIMITS_MB";
    public static final String ROLES = "ROLES";
    public static final String OD_ROLES_MB = "OD_ROLES_MB";
    public static final String OD_USER_ROLES_MAP_MB = "OD_USER_ROLES_MAP_MB";
    public static final String RULES = "RULES";
    public static final String OD_RULES_MB = "OD_RULES_MB";
    public static final String OD_RULES_DEF_MB = "OD_RULES_DEF_MB";
    public static final String OD_PARSED_RULE_MB = "OD_PARSED_RULE_MB";
    public static final String OD_RULES_ACC_MAP_MB = "OD_RULES_ACC_MAP_MB";
    public static final String BENEFICIARY_MAINTENANCE = "BENEFICIARY_MAINTENANCE";
    public static final String CIM_SUBPROD_ATTR_MAP_MB = "CIM_SUBPROD_ATTR_MAP_MB";

    public static final int RECORDS_PER_BATCH = 500;

}
