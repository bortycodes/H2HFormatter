package com.bdo.cms.bob_data_migration_utility.domain;

import java.util.Map;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFunction {

    private String od_gcif;
    private String od_user_no;
    private String od_function_code;
    private String od_product_code;
    private String od_subprod_code;
    private String od_acc_no;
    private String criteria_type;
    private String unit_id;
    private int role;
    private Map<String, String> rs;
    
}
