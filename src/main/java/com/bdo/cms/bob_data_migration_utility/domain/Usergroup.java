package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

import java.util.Date;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Usergroup {
    private String odUsergroupCode;
    private String odGcif;
    private String odUsergroupName;
    private String odUsergroupDesc;
    private String odMakerId;
    private Date odMakerDate;
    private String odAuthId;
    private Date odAuthDate;
    private String odStatus;
    private String odMakerName;
    private String odAuthName;
    private String odUsergroupType;
}
