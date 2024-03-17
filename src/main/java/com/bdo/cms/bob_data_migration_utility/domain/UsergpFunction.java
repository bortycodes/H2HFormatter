package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsergpFunction {
    private String odUsergroupCode;
    private String odFunctionCode;
    private String odProductCode;
    private String odSubprodCode;
    private String odAccNo;
    private String odRollbackFlag;
    private String criteriaType;
    private String unitId;
    private int role;
}
