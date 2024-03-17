package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryMaintenance {
    String referenceNo;
    String txnStatus;
    String custId;
    String businessProdCode;
    String beneAccNo;
    String beneName;
    String aliasName;
    String beneAccType;
    String beneBankNm;
    String beneBranchNm;
    String beneBankNmInter;
    String beneAddress1;

}
