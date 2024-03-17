/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bdo.cms.bob_data_migration_utility.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

/**
 *
 * @author a025012567
 */
@Configuration
public class AppConfig {
    
    @Value("${app.version}")
    public String version;
    
    @Value("${app.build.release}")
    public String build_release;
    
}
