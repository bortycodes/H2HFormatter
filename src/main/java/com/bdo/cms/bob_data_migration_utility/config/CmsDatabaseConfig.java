package com.bdo.cms.bob_data_migration_utility.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("file:${bobdmu.cms.db.properties}")
public class CmsDatabaseConfig {

    @Value("${jdbc.driverClass.oracle}")
    private String driverClass;

    @Value("${jdbc.jdbcUrl.cms}")
    private String url;

    @Value("${jdbc.user.bob.cms}")
    private String username;

    @Value("${jdbc.password.cms}")
    private String password;

    @Bean
    public BasicDataSource cmsDb() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setUrl(url);
        basicDataSource.setUsername(username);
        basicDataSource.setPassword(password);
        return basicDataSource;
    }
}
