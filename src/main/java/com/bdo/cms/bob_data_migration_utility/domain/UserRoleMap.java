package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleMap {
    private String odUserNo;
    private String odGcif;
    private String odRoleLevel;
    private String odUsergroupCode;
    private String unitId;
    private String userType;
    private int role;
}
