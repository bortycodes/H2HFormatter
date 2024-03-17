package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class CgConvert {
    private String cmsCgId;
    private String bobCgId;
    private String corpCd;
}
