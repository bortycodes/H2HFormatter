package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleContainer {
    private String corpGroupId;
    private String userCd;
    private int role;
    private String roleDesc;
    private String levelClass;
}
