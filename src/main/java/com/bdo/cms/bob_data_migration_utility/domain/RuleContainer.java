package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RuleContainer {
    private int ruleId;
    private String dscp;
    private String matId;
    private String matRuleId;
    private BigDecimal lowerLimit;
    private BigDecimal upperLimit;
    private String rules;
    private int role;
}
