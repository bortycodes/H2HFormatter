package com.bdo.cms.bob_data_migration_utility.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FieldsAndTablesConfig {
    
    @Value("${tables.cms.raw}")
    public List<String> cmsRawTables;
	
    @Value("${tables.names}")
    public List<String> tableNames;

    @Value("${fields.cimCustDefnMbFields}")
    public List<String> cimCustDefnMbFields;

    @Value("${fields.cimCustAcctMbFields}")
    public List<String> cimCustAcctMbFields;

    @Value("${fields.cimCustContactInfoMbFields}")
    public List<String> cimCustContactInfoMbFields;

    @Value("${fields.cimDomainDefnFields}")
    public List<String> cimDomainDefnFields;

    @Value("${fields.cimCustomerLimitMbFields}")
    public List<String> cimCustomerLimitMbFields;
    
    @Value("${fields.odCorporateLimitsMb}")
    public List<String> odCorporateLimitsMbFields;

    @Value("${fields.odUsergroupMbFields}")
    public List<String> odUsergroupMbFields;
    
    @Value("${fields.odCorporateFunctionMb}")
    public List<String> odCorporateFunctionMbFields;

    @Value("${fields.odUsersMbFields}")
    public List<String> odUsersMbFields;
    
    @Value("${fields.odUserLimitsMb}")
    public List<String> odUserLimitsMbFields;

    @Value("${fields.orbiibsNicknameFields}")
    public List<String> orbiibsNicknameFields;

    @Value("${fields.odUserFunctionMbFields}")
    public List<String> odUserFunctionMbFields;

    @Value("${fields.odUsergpFunctionMbFields}")
    public List<String> odUsergpFunctionMbFields;

    @Value("${fields.odUsergpLimitsMbFields}")
    public List<String> odUsergpLimitsMbFields;

    @Value("${fields.odRolesMbFields}")
    public List<String> odRolesMbFields;

    @Value("${fields.odRulesMbFields}")
    public List<String> odRulesMbFields;

    @Value("${fields.odRulesDefMbFields}")
    public List<String> odRulesDefMbFields;

    @Value("${fields.odParsedRuleMbFields}")
    public List<String> odParsedRuleMbFields;

    @Value("${fields.odRulesAccMapMbFields}")
    public List<String> odRulesAccMapMbFields;

    @Value("${fields.odUserRolesMapMbFields}")
    public List<String> odUserRolesMapMbFields;

    @Value("${fields.beneficiaryMaintenanceFields}")
    public List<String> beneficiaryMaintenanceFields;

    @Value("${paymnt.products.cimPaymentBkftDtMb.fields}")
    public List<String> paymntProductsCimPaymentBkftDtMbFields;    
    
    @Value("${paymnt.products.cimPaymentParamsMb.fields}")
    public List<String> paymntProductsCimPaymentParamsMbFields;    
    
    @Value("${paymnt.products.cimBusinessParamsMb.fields}")
    public List<String> paymntProductsCimBusinessParamsMbFields;    
    
    @Value("${fields.cimSubProdAttrMapMbFields}")
    public List<String> cimSubProdAttrMapMbFields;
    
    // SUBPROD for PAYMNT
//    @Value("${paymnt.subprod.ada}")
//    public List<String> ada;

    @Value("${paymnt.subprod.a2atp}")
    public List<String> a2atp;

    @Value("${paymnt.subprod.a2aself}")
    public List<String> a2aself;

    @Value("${paymnt.subprod.dft.outward}")
    public List<String> dft_outward;
    
    @Value("${paymnt.subprod.dft.wired}")
    public List<String> dft_wired;

    @Value("${paymnt.subprod.payroll}")
    public List<String> payroll;

    @Value("${paymnt.subprod.cbft}")
    public List<String> cbft;
    
    @Value("${paymt.subprod.bene}")
    public List<String> bene;
    
    @Value("${paymt.subprod.billpay}")
    public List<String> billpay;

}
