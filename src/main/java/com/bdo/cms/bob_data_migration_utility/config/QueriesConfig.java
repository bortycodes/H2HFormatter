package com.bdo.cms.bob_data_migration_utility.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:${bobdmu.config.location}/queries.properties")
public class QueriesConfig {
    @Value("${query.delete}")
    public  String delete;

    @Value("${query.check}")
    public  String selectProcessed;

    @Value("${generate.save}")
    public String gnrtSaveInputs;

    @Value("${migrate.retrieve}")
    public String mgrtRetrieve;

    @Value("${migrate.update}")
    public String mgrtUpdateInputs;

    @Value("${convert.usergroup.first}")
    public String cnvrtFirstSelect;

    @Value("${convert.usergroup.check}")
    public String cnvrtCheckSelect;

    @Value("${convert.usergroup.fetch}")
    public String cnvrtFetch;

    // select queries for GENERATE
    @Value("${generate.select.cimCustDefnMb}")
    public String gnrtSelectCimCustDefnMb;

    @Value("${generate.select.cimCustAcctMb}")
    public String gnrtSelectCimCustAcctMb;

    @Value("${generate.select.cimCustContactInfoMb}")
    public String gnrtSelectCimCustContactInfoMb;
    
    @Value("${generate.select.cimCustBillingDetailsMb.other_account}")
    public String gnrtSelectCimCustBillingDetailsMbOtherAccount;
    
    @Value("${generate.select.cimCustBillingDetailsMb}")
    public String gnrtSelectCimCustBillingDetailsMb;
            
    @Value("${generate.select.cimPaymentBkftDtMb}")
    public String gnrtSelectCimPaymentBkftDtMb;
            
    @Value("${generate.select.cimPaymentCustPrefMb}")
    public String gnrtSelectCimPaymentCustPrefMb;
            
    @Value("${generate.select.cimPaymentParamsMb}")
    public String gnrtSelectCimPaymentParamsMb;
            
    @Value("${generate.select.cimBusinessParamsMb}")
    public String gnrtSelectCimBusinessParamsMb;

    @Value("${generate.select.cimDomainDefn}")
    public String gnrtSelectCimDomainDefn;

    @Value("${generate.select.cimCustomerLimitMb}")
    public String gnrtSelectCimCustomerLimitMb;
    
    @Value("${generate.select.odCorporateLimitsMb}")
    public String gnrtSelectOdCorporateLimitsMb;

    @Value("${generate.select.odUsergroupMb}")
    public String gnrtSelectOdUsergroupMb;

    @Value("${generate.select.odCorporateFunctionMb}")
    public String gnrtSelectOdCorporateFunctionMb;

    @Value("${generate.select.odUsersMb}")
    public String gnrtSelectOdUsersMb;
    
    @Value("${generate.select.odUserLimitsMb}")
    public String gnrtSelectOdUserLimitsMb;

    @Value("${generate.select.orbiibsNickname}")
    public String gnrtSelectOrbiibsNickname;

    @Value("${generate.select.odUserFunctionMb}")
    public String gnrtSelectOdUserFunctionMb;

    @Value("${generate.select.odUsergpFunctionMb}")
    public String gnrtSelectOdUsergpFunctionMb;

    @Value("${generate.select.odUsergpLimitsMb}")
    public String gnrtSelectOdUsergpLimitsMb;

    @Value("${generate.select.roles}")
    public String gnrtSelectRoles;

    @Value("${generate.select.rules}")
    public String gnrtSelectRules;

    @Value("${generate.select.rulesAcct}")
    public String gnrtSelectRulesAcct;

    @Value("${generate.select.userRoles}")
    public String gnrtSelectUserRoles;

    @Value("${generate.select.beneficiaryMaintenance}")
    public String gnrtSelectBeneficiaryMaintenance;
        
    @Value("${generate.select.cimSubProdAttrMapMb}")
    public String gnrtSelectCimSubProdAttrMapMb;

    // select queries for MIGRATE
    @Value("${migrate.select.cimCustDefnMb}")
    public String mgrtSelectCimCustDefnMb;

    @Value("${migrate.select.cimCustAcctMb}")
    public String mgrtSelectCimCustAcctMb;

    @Value("${migrate.select.cimCustContactInfoMb}")
    public String mgrtSelectCimCustContactInfoMb;

    @Value("${migrate.select.cimDomainDefn}")
    public String mgrtSelectCimDomainDefn;

    @Value("${migrate.select.cimCustomerLimitMb}")
    public String mgrtSelectCimCustomerLimitMb;
    
    @Value("${migrate.select.odCorporateLimitsMb}")
    public String mgrtSelectOdCorporateLimitsMb;

    @Value("${migrate.select.odUsergroupMb}")
    public String mgrtSelectOdUsergroupMb;

    @Value("${migrate.select.odCorporateFunctionMb}")
    public String mgrtSelectOdCorporateFunctionMb;

    @Value("${migrate.select.odUsersMb}")
    public String mgrtSelectOdUsersMb;
    
    @Value("${migrate.select.odUserLimitsMb}")
    public String mgrtSelectOdUserLimitsMb;

    @Value("${migrate.select.orbiibsNickname}")
    public String mgrtSelectOrbiibsNickname;

    @Value("${migrate.select.odUserFunctionMb}")
    public String mgrtSelectOdUserFunctionMb;

    @Value("${migrate.select.odUsergpFunctionMb}")
    public String mgrtSelectOdUsergpFunctionMb;

    @Value("${migrate.select.odUsergpLimitsMb}")
    public String mgrtSelectOdUsergpLimitsMb;

    @Value("${migrate.select.odRolesMb}")
    public String mgrtSelectOdRolesMb;

    @Value("${migrate.select.odRulesMb}")
    public String mgrtSelectOdRulesMb;

    @Value("${migrate.select.odRulesDefMb}")
    public String mgrtSelectOdRulesDefMb;

    @Value("${migrate.select.odParsedRuleMb}")
    public String mgrtSelectOdParsedRuleMb;

    @Value("${migrate.select.odRulesAccMapMb}")
    public String mgrtSelectOdRulesAccMapMb;

    @Value("${migrate.select.odUserRolesMapMb}")
    public String mgrtSelectOdUserRolesMapMb;

    @Value("${migrate.select.beneficiaryMaintenance}")
    public String mgrtSelectBeneficiaryMaintenance;
    
    @Value("${migrate.select.cimCustBillingDetailsMb}")
    public String mgrtSelectCimCustBillingDetailsMb;
    
    @Value("${migrate.select.cimPaymentBkftDtMb}")
    public String mgrtSelectCimPaymentBkftDtMb;
    
    @Value("${migrate.select.cimPaymentCustPrefMb}")
    public String mgrtSelectCimPaymentCustPrefMb;
    
    @Value("${migrate.select.cimPaymentParamsMb}")
    public String mgrtSelectCimPaymentParamsMb;
    
    @Value("${migrate.select.cimBusinessParamsMb}")
    public String mgrtSelectCimBusinessParamsMb;
    
    @Value("${migrate.select.cimSubProdAttrMapMb}")
    public String mgrtSelectCimSubProdAttrMapMb;
    
    @Value("${query.specialcharacter.replacement}")
    public String specialCharactersMap;

}
