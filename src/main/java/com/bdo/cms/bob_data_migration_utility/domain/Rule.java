package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Rule {
    private String odGcif;
    private int odRuleId;
    private String odRuleName;
    private String odRuleDesc;
    private String rule_matrix_id;
}
