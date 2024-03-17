package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CorporateFunction {
    private String odGcif;
    private String odFunctionCode;
    private String odProductCode;
    private String odSubprodCode;
    private String verificationReq;
    private String releaseReq;

}
