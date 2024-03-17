package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ParsedRule {
    private int odRuleParseId;
    private String odLevel;
    private int odCount;
    private String approvalFlow;
}
