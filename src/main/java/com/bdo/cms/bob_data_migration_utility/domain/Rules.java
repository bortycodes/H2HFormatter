package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Rules {
    private List<Rule> ruleList;
    private List<RulesDef> rulesDefList;
    private List<ParsedRule> parsedRuleList;
    private List<RulesAccMap> rulesAccMapList;
}
