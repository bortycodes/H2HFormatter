package com.bdo.cms.bob_data_migration_utility.domain;


import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FunctionId {
    private String funcId;
    private List<Integer> wfModels;
}
