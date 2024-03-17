package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RuleAcctContainer {
    private String matrixId;
    private String tranMatrixId;
    private String funcId;
    private String currencyCd;
    private String acctNo;
    private int role;
}
