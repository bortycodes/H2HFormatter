package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RulesAccMap {
    private int odRuleId;
    private String odFunctionId;
    private String odProductCode;
    private String odSubproductCode;
    private String odGcif;
    private String odOrgAccNo;
    private String criteriaType;
    private String currency;
    private String unitId;

}
