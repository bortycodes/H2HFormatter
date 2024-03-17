/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bdo.cms.bob_data_migration_utility.domain;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author a025012567
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BobUser {
    
    private String login_id;
    private String org_id;
    private String domain_id;
    private UserLimits user_limits;
    private Map<String, UserFunction> user_functions;
    private Input input;
    
}
