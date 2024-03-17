package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RulesDef {
    private int odRuleId;
    private int odRuleParseId;
    private BigDecimal odMinAmt;
    private BigDecimal odMaxAmt;
    private String ruleInfo;
}
